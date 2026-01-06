package de.mecrytv.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LanguageConfigManager {
    private final Path filePath;
    private final String langCode;
    private final String category;
    private Map<String, Object> configData = new HashMap<>();
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Charset[] charsetsToTry = {
            StandardCharsets.UTF_8,
            Charset.forName("windows-1252"),
            StandardCharsets.ISO_8859_1,
            StandardCharsets.UTF_16
    };

    public LanguageConfigManager(Path langFolder, String langCode, String category) {
        this.langCode = langCode;
        this.category = category;
        this.filePath = langFolder.resolve(category + ".json");
        init();
    }

    private void init() {
        try {
            if (Files.notExists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }

            String resourcePath = "languages/" + langCode + "/" + category + ".json";

            if (Files.notExists(filePath)) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                    if (in != null) {
                        Files.copy(in, filePath);
                        load();
                    } else {
                        save();
                    }
                }
            } else {
                load();
                try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                    if (in != null) {
                        Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                        Map<String, Object> internalData = gson.fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());
                        if (internalData != null && mergeMaps(internalData, configData)) {
                            save();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean mergeMaps(Map<String, Object> source, Map<String, Object> target) {
        boolean changed = false;
        for (String key : source.keySet()) {
            Object sourceValue = source.get(key);
            Object targetValue = target.get(key);

            if (!target.containsKey(key)) {
                target.put(key, sourceValue);
                changed = true;
            } else if (sourceValue instanceof Map && targetValue instanceof Map) {
                if (mergeMaps((Map<String, Object>) sourceValue, (Map<String, Object>) targetValue)) {
                    changed = true;
                }
            }
        }
        return changed;
    }

    public void load() {
        if (Files.notExists(filePath)) return;

        boolean success = false;
        Throwable lastSyntaxError = null;

        for (Charset charset : charsetsToTry) {
            try {
                byte[] bytes = Files.readAllBytes(filePath);

                java.nio.charset.CharsetDecoder decoder = charset.newDecoder()
                        .onMalformedInput(java.nio.charset.CodingErrorAction.REPLACE)
                        .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPLACE);

                String content = decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString();

                Map<String, Object> loadedData = gson.fromJson(content, new TypeToken<Map<String, Object>>(){}.getType());

                if (loadedData != null) {
                    this.configData = loadedData;
                    success = true;
                    break;
                }
            } catch (com.google.gson.JsonSyntaxException e) {
                lastSyntaxError = e;
            } catch (Exception e) {
                continue;
            }
        }

        if (!success) {
            System.err.println("=================================================");
            System.err.println("[LanguageSystem] KRITISCHER SYNTAX-FEHLER!");
            System.err.println("Datei: " + filePath.getFileName());

            if (lastSyntaxError != null) {
                System.err.println("Fehler: " + lastSyntaxError.getMessage());
            } else {
                System.err.println("Fehler: Die Datei konnte mit keiner Kodierung gelesen werden.");
            }

            System.err.println("[LanguageSystem] Die alten Übersetzungen bleiben im RAM.");
            System.err.println("=================================================");
        }
    }

    public void save() {
        boolean success = false;
        Exception lastException = null;
        for (Charset charset : charsetsToTry) {
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, charset)) {
                gson.toJson(configData, writer);
                success = true;
                break;
            } catch (IOException e) {
                lastException = e;
            }
        }
        if (!success && lastException != null) lastException.printStackTrace();
    }

    public String getString(String path) {
        Object val = get(path);
        return (val != null) ? String.valueOf(val) : "<red>Missing key: <white>" + path;
    }

    public int getInt(String path) {
        Object val = get(path);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(val));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Object get(String path) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = configData;
        for (int i = 0; i < keys.length - 1; i++) {
            Object obj = current.get(keys[i]);
            if (obj instanceof Map) {
                current = (Map<String, Object>) obj;
            } else return null;
        }
        return current.get(keys[keys.length - 1]);
    }

    public void set(String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = configData;
        for (int i = 0; i < keys.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(keys[i], k -> new HashMap<String, Object>());
        }
        if (value == null) current.remove(keys[keys.length - 1]);
        else current.put(keys[keys.length - 1], value);
        save();
    }

    public List<String> getStringList(String path) {
        Object val = get(path);
        if (val instanceof List) {
            List<String> list = (List<String>) val;
            list.replaceAll(s -> s.replace("&", "§"));
            return list;
        }
        return Collections.singletonList("§cMissing list: " + path);
    }
}