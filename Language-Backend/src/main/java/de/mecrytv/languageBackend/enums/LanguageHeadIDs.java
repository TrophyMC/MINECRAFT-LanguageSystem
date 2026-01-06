package de.mecrytv.languageBackend.enums;

import java.util.Arrays;

public enum LanguageHeadIDs {
    GERMANY("de_DE", "522"),
    UNITED_STATES("en_US", "27589");

    private final String langCode;
    private final String hdbId;

    LanguageHeadIDs(String langCode, String hdbId) {
        this.langCode = langCode;
        this.hdbId = hdbId;
    }

    public String getLangCode() { return langCode; }
    public String getHdbId() { return hdbId; }

    public static String getHdbIdByCode(String code) {
        return Arrays.stream(values())
                .filter(head -> head.getLangCode().equalsIgnoreCase(code))
                .map(LanguageHeadIDs::getHdbId)
                .findFirst()
                .orElse(null);
    }
}