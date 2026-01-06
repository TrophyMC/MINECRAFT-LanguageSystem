package de.mecrytv.languageBackend;

import de.mecrytv.LanguageAPI;
import de.mecrytv.languageBackend.cache.CacheService;
import de.mecrytv.languageBackend.mariadb.MariaDBManager;
import de.mecrytv.languageBackend.redis.RedisManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;

public final class LanguageBackend extends JavaPlugin {

    private static LanguageBackend instance;
    private LanguageAPI languageAPI;
    private BukkitAudiences adventure;
    private RedisManager redisManager;
    private MariaDBManager mariaDBManager;
    private CacheService cacheService;

    @Override
    public void onEnable() {
        instance = this;
        this.languageAPI = new LanguageAPI(Paths.get("/home/minecraft/languages/"));
        this.adventure = BukkitAudiences.create(this);

        this.redisManager = new RedisManager();
        this.mariaDBManager = new MariaDBManager();
        this.cacheService = new CacheService();
        this.cacheService.initialize();
    }

    @Override
    public void onDisable() {
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
        if (redisManager != null) redisManager.disconnect();
        if (mariaDBManager != null) mariaDBManager.shutDown();
    }

    public static LanguageBackend getInstance() { return instance; }
    public LanguageAPI getLanguageAPI() { return languageAPI; }
    public BukkitAudiences adventure() { return adventure; }
    public RedisManager getRedisManager() {
        return redisManager;
    }
    public MariaDBManager getMariaDBManager() {
        return mariaDBManager;
    }
    public CacheService getCacheService() {
        return cacheService;
    }
}
