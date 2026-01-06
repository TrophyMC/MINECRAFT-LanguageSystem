package de.mecrytv.languageBackend;

import de.mecrytv.LanguageAPI;
import de.mecrytv.languageBackend.cache.CacheService;
import de.mecrytv.languageBackend.commands.LanguageCommand;
import de.mecrytv.languageBackend.listeners.JoinListener;
import de.mecrytv.languageBackend.listeners.LanguageListener;
import de.mecrytv.languageBackend.manager.ConfigManager;
import de.mecrytv.languageBackend.mariadb.MariaDBManager;
import de.mecrytv.languageBackend.redis.RedisManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;

public final class LanguageBackend extends JavaPlugin {

    private static LanguageBackend instance;
    private LanguageAPI languageAPI;
    private BukkitAudiences adventure;
    private RedisManager redisManager;
    private MariaDBManager mariaDBManager;
    private CacheService cacheService;
    private ConfigManager config;

    @Override
    public void onEnable() {
        instance = this;
        this.languageAPI = new LanguageAPI(Paths.get("/home/minecraft/languages/"));
        this.adventure = BukkitAudiences.create(this);

        this.config = new ConfigManager(getDataFolder().toPath(), "config.json");

        this.redisManager = new RedisManager();
        this.mariaDBManager = new MariaDBManager();
        this.cacheService = new CacheService();
        this.cacheService.initialize();

        if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
            getLogger().info("✅ HeadDatabase-API erfolgreich gefunden!");
        } else {
            getLogger().warning("❌ HeadDatabase wurde nicht gefunden! GUI-Flaggen funktionieren nicht.");
        }

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            getLogger().info("🔄 Automatischer Datenbank-Sync wird ausgeführt...");
            this.cacheService.flushAll();
        }, 1200L, 6000L);

        getCommand("language").setExecutor(new LanguageCommand());
        getServer().getPluginManager().registerEvents(new LanguageListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
    }

    @Override
    public void onDisable() {
        if (cacheService != null) {
            getLogger().info("💾 Letzter Datenbank-Sync vor dem Beenden...");
            cacheService.flushAll();
        }

        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
        if (redisManager != null) redisManager.disconnect();
        if (mariaDBManager != null) mariaDBManager.shutDown();
    }

    public Component getPrefix() {
        String prefixRaw = this.config.getString("prefix");
        if (prefixRaw.isEmpty() || prefixRaw == null) {
            prefixRaw = "<darK_grey>[<gold>Language<dark_grey>] ";
        }
        return MiniMessage.miniMessage().deserialize(prefixRaw);
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
    public ConfigManager getConfiguration() {
        return config;
    }
}
