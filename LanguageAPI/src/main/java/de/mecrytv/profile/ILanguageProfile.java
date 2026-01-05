package de.mecrytv.profile;

import java.util.Map;
import java.util.UUID;

public interface ILanguageProfile {

    UUID getUniqueId();
    String getLanguageCode();
    void setLanguageCode(String langCode);
    String translate(String key);
    String translate(String key, Map<String, String> placeholders);
}
