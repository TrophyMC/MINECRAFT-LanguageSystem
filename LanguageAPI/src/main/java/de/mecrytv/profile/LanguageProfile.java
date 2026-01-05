package de.mecrytv.profile;

import de.mecrytv.LanguageAPI;

import java.util.Map;
import java.util.UUID;

public class LanguageProfile implements ILanguageProfile {

    private final UUID uuid;
    private String languageCode;
    private final LanguageAPI languageAPI;

    public LanguageProfile(UUID uuid, String languageCode, LanguageAPI languageAPI) {
        this.uuid = uuid;
        this.languageCode = languageCode;
        this.languageAPI = languageAPI;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public String getLanguageCode() {
        return languageCode;
    }

    @Override
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    @Override
    public String translate(String key) {
        return languageAPI.getTranslation(languageCode, key);
    }

    @Override
    public String translate(String key, Map<String, String> placeholders) {
        String message = languageAPI.getTranslation(languageCode, key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return message;
    }
}
