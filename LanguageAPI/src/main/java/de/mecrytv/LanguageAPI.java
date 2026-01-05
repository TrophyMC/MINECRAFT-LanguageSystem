package de.mecrytv;

import de.mecrytv.manager.LanguageConfigManager;
import de.mecrytv.profile.ILanguageProfile;
import de.mecrytv.profile.LanguageProfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class LanguageAPI {

    private final Map<String, Map<String, LanguageConfigManager>> configs = new HashMap<>();
    private final Map<UUID, ILanguageProfile> profileCache = new HashMap<>();
    private final List<String> categories = Arrays.asList("commands", "listeners", "messages", "gui");
    private final Path dataFolder;

    public LanguageAPI(Path dataFolder) {
        this.dataFolder = dataFolder;
        loadAvailableLanguages();
    }

    public boolean reloadAll() {
        try {
            configs.clear();
            loadAvailableLanguages();

            System.out.println("[LanguageAPI] Alle Sprachen wurden neu geladen.");
            return !configs.isEmpty();
        } catch (Exception e) {
            System.err.println("[LanguageAPI] Fehler beim Neuladen der Sprachen!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean reloadCategory(String langCode, String category) {
        Map<String, LanguageConfigManager> langMap = configs.get(langCode);
        if (langMap != null) {
            LanguageConfigManager manager = langMap.get(category);
            if (manager != null) {
                manager.load();
                System.out.println("[LanguageAPI] Kategorie '" + category + "' für " + langCode + " wurde neu geladen.");
                return true;
            }
        }
        return false;
    }

    public boolean reloadLanguage(String langCode) {
        Map<String, LanguageConfigManager> langMap = configs.get(langCode);
        if (langMap != null) {
            langMap.values().forEach(LanguageConfigManager::load);
            System.out.println("[LanguageAPI] Alle Dateien für " + langCode + " wurden neu geladen.");
            return true;
        }
        return false;
    }

    public void loadAvailableLanguages() {
        if (!Files.exists(dataFolder)) {
            try {
                Files.createDirectories(dataFolder);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try (Stream<Path> paths = Files.list(dataFolder)) {
            paths.filter(Files::isDirectory).forEach(langDir -> {
                String langCode = langDir.getFileName().toString();
                Map<String, LanguageConfigManager> categoryMap = new HashMap<>();

                for (String category : categories) {
                    categoryMap.put(category, new LanguageConfigManager(langDir, langCode, category));
                }

                configs.put(langCode, categoryMap);
                System.out.println("[LanguageAPI] Sprache geladen: " + langCode + " (" + categories.size() + " Dateien)");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ILanguageProfile getProfile(UUID uuid, String initialLang) {
        return profileCache.computeIfAbsent(uuid, k -> new LanguageProfile(uuid, initialLang, this));
    }

    public String getTranslation(String langCode, String fullKey) {
        String[] split = fullKey.split("\\.", 2);
        if (split.length < 2) return "§cInvalid Key Format: " + fullKey;

        String category = split[0];
        String key = split[1];

        Map<String, LanguageConfigManager> categoriesForLang = configs.get(langCode);

        if (categoriesForLang == null) {
            categoriesForLang = configs.get("en_US");
        }

        if (categoriesForLang != null) {
            LanguageConfigManager manager = categoriesForLang.get(category);
            if (manager != null) {
                return manager.getString(key);
            }
        }

        return "§cMissing Lang: " + langCode + " | " + fullKey;
    }

    public void removeProfile(UUID uuid) {
        profileCache.remove(uuid);
    }

    public Map<String, Map<String, LanguageConfigManager>> getConfigs() {
        return configs;
    }
}