package de.mecrytv.languageBackend.cache;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.mecrytv.languageBackend.LanguageBackend;
import de.mecrytv.languageBackend.models.ICacheModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public abstract class CacheNode<T extends ICacheModel> {
    protected final String nodeName;
    protected final Supplier<T> factory;
    protected final Gson gson = new Gson();
    protected final String redisPrefix;
    protected final String dirtySetKey;
    protected final String deletedSetKey;

    public CacheNode(String nodeName, Supplier<T> factory) {
        this.nodeName = nodeName;
        this.factory = factory;
        this.redisPrefix = "cache:" + nodeName + ":";
        this.dirtySetKey = "dirty:" + nodeName;
        this.deletedSetKey = "deleted:" + nodeName;
    }

    public void set(T model) {
        String key = redisPrefix + model.getIdentifier();
        LanguageBackend.getInstance().getRedisManager().set(key, model.serialize().toString());

        LanguageBackend.getInstance().getRedisManager().srem(deletedSetKey, model.getIdentifier());
        LanguageBackend.getInstance().getRedisManager().sadd(dirtySetKey, model.getIdentifier());
    }

    public T get(String identifier) {
        if (LanguageBackend.getInstance().getRedisManager().sismember(deletedSetKey, identifier)) {
            return null;
        }

        String json = LanguageBackend.getInstance().getRedisManager().get(redisPrefix + identifier);
        if (json != null) {
            T model = factory.get();
            model.deserialize(gson.fromJson(json, JsonObject.class));
            return model;
        }

        T dbModel = loadFromDatabase(identifier);
        if (dbModel != null) {
            set(dbModel);
            return dbModel;
        }
        return null;
    }

    public void flush() {
        java.util.Set<String> toSave = LanguageBackend.getInstance().getRedisManager().smembers(dirtySetKey);
        if (!toSave.isEmpty()) {
            try (Connection conn = LanguageBackend.getInstance().getMariaDBManager().getConnection()) {
                for (String id : toSave) {
                    T model = get(id);
                    if (model != null) {
                        saveToDatabase(conn, model);
                        LanguageBackend.getInstance().getRedisManager().srem(dirtySetKey, id);
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }

        java.util.Set<String> toDelete = LanguageBackend.getInstance().getRedisManager().smembers(deletedSetKey);
        if (!toDelete.isEmpty()) {
            System.out.println("🗑️ Lösche " + toDelete.size() + " Einträge (" + nodeName + ")...");
            try (Connection conn = LanguageBackend.getInstance().getMariaDBManager().getConnection()) {
                for (String id : toDelete) {
                    removeFromDatabase(conn, id);
                    LanguageBackend.getInstance().getRedisManager().srem(deletedSetKey, id);
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public void delete(String identifier) {
        String key = redisPrefix + identifier;

        LanguageBackend.getInstance().getRedisManager().del(key);
        LanguageBackend.getInstance().getRedisManager().srem(dirtySetKey, identifier);
        LanguageBackend.getInstance().getRedisManager().sadd(deletedSetKey, identifier);
    }

    public java.util.List<T> getAll() {
        java.util.Map<String, T> mergedData = new java.util.HashMap<>();

        for (T dbModel : getAllFromDatabase()) {
            if (!LanguageBackend.getInstance().getRedisManager().sismember(deletedSetKey, dbModel.getIdentifier())) {
                mergedData.put(dbModel.getIdentifier(), dbModel);
            }
        }

        java.util.Set<String> redisKeys = LanguageBackend.getInstance().getRedisManager().keys(redisPrefix + "*");
        for (String key : redisKeys) {
            String identifier = key.replace(redisPrefix, "");
            T redisModel = get(identifier);

            if (redisModel != null) {
                mergedData.put(identifier, redisModel);
            }
        }

        return new java.util.ArrayList<>(mergedData.values());
    }

    public abstract void createTableIfNotExists();
    public abstract java.util.List<T> getAllFromDatabase();
    protected abstract void removeFromDatabase(Connection conn, String identifier) throws SQLException;
    protected abstract void saveToDatabase(Connection conn, T model) throws SQLException;
    protected abstract T loadFromDatabase(String identifier);
}