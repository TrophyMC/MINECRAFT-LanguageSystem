package de.mecrytv.languageVelocity.cache;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.mecrytv.languageVelocity.LanguageVelocity;
import de.mecrytv.languageVelocity.models.ICacheModel;
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
        LanguageVelocity.getInstance().getServiceManager().getRedisManager().set(key, model.serialize().toString());

        LanguageVelocity.getInstance().getServiceManager().getRedisManager().srem(deletedSetKey, model.getIdentifier());
        LanguageVelocity.getInstance().getServiceManager().getRedisManager().sadd(dirtySetKey, model.getIdentifier());
    }

    public T get(String identifier) {
        if (LanguageVelocity.getInstance().getServiceManager().getRedisManager().sismember(deletedSetKey, identifier)) {
            return null;
        }

        String json = LanguageVelocity.getInstance().getServiceManager().getRedisManager().get(redisPrefix + identifier);
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
        java.util.Set<String> toSave = LanguageVelocity.getInstance().getServiceManager().getRedisManager().smembers(dirtySetKey);
        if (!toSave.isEmpty()) {
            try (Connection conn = LanguageVelocity.getInstance().getServiceManager().getMariaDBManager().getConnection()) {
                for (String id : toSave) {
                    T model = get(id);
                    if (model != null) {
                        saveToDatabase(conn, model);
                        LanguageVelocity.getInstance().getServiceManager().getRedisManager().srem(dirtySetKey, id);
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }

        java.util.Set<String> toDelete = LanguageVelocity.getInstance().getServiceManager().getRedisManager().smembers(deletedSetKey);
        if (!toDelete.isEmpty()) {
            LanguageVelocity.getInstance().getLogger().info("🗑️ Lösche " + toDelete.size() + " Einträge (" + nodeName + ")...");
            try (Connection conn = LanguageVelocity.getInstance().getServiceManager().getMariaDBManager().getConnection()) {
                for (String id : toDelete) {
                    removeFromDatabase(conn, id);
                    LanguageVelocity.getInstance().getServiceManager().getRedisManager().srem(deletedSetKey, id);
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public void delete(String identifier) {
        String key = redisPrefix + identifier;

        LanguageVelocity.getInstance().getServiceManager().getRedisManager().del(key);
        LanguageVelocity.getInstance().getServiceManager().getRedisManager().srem(dirtySetKey, identifier);
        LanguageVelocity.getInstance().getServiceManager().getRedisManager().sadd(deletedSetKey, identifier);
    }

    public java.util.List<T> getAll() {
        java.util.Map<String, T> mergedData = new java.util.HashMap<>();

        for (T dbModel : getAllFromDatabase()) {
            if (!LanguageVelocity.getInstance().getServiceManager().getRedisManager().sismember(deletedSetKey, dbModel.getIdentifier())) {
                mergedData.put(dbModel.getIdentifier(), dbModel);
            }
        }

        java.util.Set<String> redisKeys = LanguageVelocity.getInstance().getServiceManager().getRedisManager().keys(redisPrefix + "*");
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