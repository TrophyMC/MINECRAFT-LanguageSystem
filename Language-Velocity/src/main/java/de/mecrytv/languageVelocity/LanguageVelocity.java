package de.mecrytv.languageVelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.mecrytv.LanguageAPI;
import de.mecrytv.languageVelocity.manager.ServiceManager;
import de.mecrytv.languageVelocity.utils.LogWithColor;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;

@Plugin(
    id = "language-velocity",
    name = "Language-Velocity",
    version = "1.0.0",
    description = "A language plugin for Velocity",
    authors = {"MecryTv"}
)
public class LanguageVelocity {

    private static LanguageVelocity instance;
    private final Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;
    private final ServiceManager serviceManager;

    private LanguageAPI languageAPI;

    @Inject
    public LanguageVelocity(Logger logger, ProxyServer server, @DataDirectory Path dataDirectory) {
        instance = this;
        this.logger = logger;
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.serviceManager = new ServiceManager(this);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        startLog();

        Path centralLangPath = Paths.get("/home/minecraft/languages/");
        this.languageAPI = new LanguageAPI(centralLangPath);

        serviceManager.initializeServices();
        serviceManager.registerAll();
        serviceManager.reload();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        serviceManager.shutdownServices();
    }

    private void startLog() {
        String[] langVeloLogo = {
                "██╗      █████╗ ███╗   ██╗ ██████╗      ██╗   ██╗███████╗██╗      ██████╗ ",
                "██║     ██╔══██╗████╗  ██║██╔════╝      ██║   ██║██╔════╝██║     ██╔═══██╗",
                "██║     ███████║██╔██╗ ██║██║  ███╗█████╗██║   ██║█████╗  ██║     ██║   ██║",
                "██║     ██╔══██║██║╚██╗██║██║   ██║╚════╝╚██╗ ██╔╝██╔══╝  ██║     ██║   ██║",
                "███████╗██║  ██║██║ ╚████║╚██████╔╝       ╚████╔╝ ███████╗███████╗╚██████╔╝",
                "╚══════╝╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝         ╚═══╝  ╚══════╝╚══════╝ ╚═════╝ ",
                "                                                                           ",
                "                            Language-Velocity                              ",
                "                            Running on Velocity                            "
        };

        for (String line : langVeloLogo) {
            logger.info(LogWithColor.color(line, LogWithColor.GREEN));
        }

        logger.info(LogWithColor.color("Developed by MecryTv", LogWithColor.GOLD));
        logger.info(LogWithColor.color("Plugin has been enabled!", LogWithColor.GREEN));
    }

    public static LanguageVelocity getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public LanguageAPI getLanguageAPI() {
        return languageAPI;
    }
}
