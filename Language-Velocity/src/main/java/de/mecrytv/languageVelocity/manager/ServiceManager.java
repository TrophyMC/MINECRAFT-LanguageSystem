package de.mecrytv.languageVelocity.manager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import de.mecrytv.languageVelocity.LanguageVelocity;
import de.mecrytv.languageVelocity.cache.CacheService;
import de.mecrytv.languageVelocity.commands.LangReloadCommand;
import de.mecrytv.languageVelocity.commands.LanguageCommand;
import de.mecrytv.languageVelocity.listener.JoinListener;
import de.mecrytv.languageVelocity.mariadb.MariaDBManager;
import de.mecrytv.languageVelocity.redis.RedisManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

public class ServiceManager {

    private final LanguageVelocity plugin;

    private ConfigManager config;
    private MariaDBManager mariaDBManager;
    private RedisManager redisManager;
    private CacheService cacheService;
    private Component prefix;

    public ServiceManager(LanguageVelocity plugin) {
        this.plugin = plugin;
    }

    public void initializeServices() {
        this.config = new ConfigManager(plugin.getDataDirectory(), "config.json");

        String prefixString = config.getString("prefix");
        this.prefix = MiniMessage.miniMessage().deserialize(prefixString);

        this.mariaDBManager = new MariaDBManager();
        this.redisManager = new RedisManager();
        this.cacheService = new CacheService();
        this.cacheService.initialize();
    }

    public void registerAll() {
        registerListener(new JoinListener());

        registerCommand(new LangReloadCommand(), "langreload");
        registerCommand(new LanguageCommand(), "language", "lang");

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            if (cacheService != null) cacheService.flushAll();
        }).repeat(1, TimeUnit.MINUTES).schedule();
    }

    public void shutdownServices() {
        Logger logger = plugin.getLogger();

        logger.info("");
        logger.info("  [ProxyTools] Shutting down services...");
        logger.info("  » Cache:   Flushing data...   [CLEANED]");
        if (cacheService != null) cacheService.flushAll();

        logger.info("  » Redis:   Disconnecting...   [OFFLINE]");
        if (redisManager != null) redisManager.disconnect();

        logger.info("  » MariaDB: Closing pool...    [CLOSED]");
        if (mariaDBManager != null) mariaDBManager.shutDown();

        logger.info("");
        logger.info("  ProxyTools has been disabled safely. Goodbye!");
        logger.info("");
    }

    public void reload() {
        String prefixString = config.getString("prefix");
        this.prefix = MiniMessage.miniMessage().deserialize(prefixString);
    }

    private void registerCommand(SimpleCommand command, String label, String... aliases) {
        CommandManager commandManager = plugin.getServer().getCommandManager();
        commandManager.register(
                commandManager.metaBuilder(label)
                        .aliases(aliases)
                        .build(),
                command
        );
    }

    private void registerListener(Object listener) {
        plugin.getServer().getEventManager().register(plugin, listener);
    }

    public ConfigManager getConfig() {
        return config;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public MariaDBManager getMariaDBManager() {
        return mariaDBManager;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public Component getPrefix() {
        return prefix;
    }

    public void sendGlobalReloadSignal() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("reload_all");

        plugin.getServer().getAllServers().forEach(server -> {
            server.sendPluginMessage(
                    MinecraftChannelIdentifier.create("network", "language"),
                    out.toByteArray()
            );
        });
    }
}
