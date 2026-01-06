package de.mecrytv.languageBackend;

import de.mecrytv.LanguageAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;

public final class LanguageBackend extends JavaPlugin {

    private static LanguageBackend instance;
    private LanguageAPI languageAPI;
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        instance = this;
        this.languageAPI = new LanguageAPI(Paths.get("/home/minecraft/languages/"));
        this.adventure = BukkitAudiences.create(this);


    }

    public static LanguageBackend getInstance() { return instance; }
    public LanguageAPI getLanguageAPI() { return languageAPI; }
    public BukkitAudiences adventure() { return adventure; }
}
