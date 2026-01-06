package de.mecrytv.languageBackend.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private final Path filePath;
    private final String fileName;
    private final String resourcePath;
    private Map<String, Object> configData = new HashMap<>();
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public ConfigManager(Path dataFolder, String fileName, String resourcePath) {
        this.filePath = dataFolder.resolve(fileName);
        this.fileName = fileName;
        this.resourcePath = resourcePath;
        init();
    }

    public ConfigManager(Path dataFolder, String fileName) {
        this(dataFolder, fileName, fileName);
    }

    private void init() {
        try {
            if (Files.notExists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }

            if (Files.notExists(filePath)) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                    if (in != null) {
                        Files.copy(in, filePath);
                    } else {
                        save();
                    }
                }
            }
            load();
        } catch (IOException e) {
            System.err.println("[LangVelo] Fehler beim Initialisieren der Config: " + fileName);
            e.printStackTrace();
        }
    }

    public void set(String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = configData;

        for (int i = 0; i < keys.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(keys[i], k -> new HashMap<String, Object>());
        }

        if (value == null) {
            current.remove(keys[keys.length - 1]);
        } else {
            current.put(keys[keys.length - 1], value);
        }
        save();
    }

    private Object get(String path) {
        String[] keys = path.split("\\.");
        Object current = configData;

        for (String key : keys) {
            if (!(current instanceof Map)) return null;
            current = ((Map<String, Object>) current).get(key);
        }
        return current;
    }

    public String getString(String path) {
        Object val = get(path);
        return (val != null) ? String.valueOf(val) : "";
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

    public List<Object> getList(String path) {
        Object val = get(path);
        if (val instanceof List) {
            return (List<Object>) val;
        }
        return new ArrayList<>();
    }

    public List<String> getStringList(String path) {
        List<Object> rawList = getList(path);
        List<String> stringList = new ArrayList<>();

        for (Object item : rawList) {
            stringList.add(String.valueOf(item));
        }
        return stringList;
    }

    public List<Integer> getIntList(String path) {
        List<Object> rawList = getList(path);
        List<Integer> intList = new ArrayList<>();

        for (Object item : rawList) {
            if (item instanceof Number) {
                intList.add(((Number) item).intValue());
            } else {
                try {
                    intList.add(Integer.parseInt(String.valueOf(item)));
                } catch (NumberFormatException ignored) {
                    // Ungültige Einträge werden übersprungen
                }
            }
        }
        return intList;
    }

    public boolean getBoolean(String path) {
        Object val = get(path);
        if (val instanceof Boolean) return (boolean) val;
        return false;
    }

    public boolean contains(String path) {
        return get(path) != null;
    }

    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            gson.toJson(configData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        if (Files.notExists(filePath)) return;

        // Erweitertes Array mit allen gängigen Kodierungen
        Charset[] charsetsToTry = {
                StandardCharsets.UTF_8,
                Charset.forName("IBM850"),     // MobaXterm / DOS Standard
                Charset.forName("windows-1252"), // Windows Standard (Westeuropa)
                Charset.forName("windows-1250"), // Windows (Mitteleuropa)
                Charset.forName("IBM437"),     // Legacy DOS
                StandardCharsets.ISO_8859_1,
                StandardCharsets.UTF_16,
                StandardCharsets.UTF_16BE,
                StandardCharsets.UTF_16LE,
                StandardCharsets.US_ASCII
        };

        boolean success = false;
        Throwable lastError = null;

        for (Charset charset : charsetsToTry) {
            try {
                // Wir lesen die Datei als Bytes ein
                byte[] bytes = Files.readAllBytes(filePath);

                // WICHTIG: Wir nutzen einen Decoder, der Fehler IGNORIERT/ERSETZT
                // Anstatt einer MalformedInputException wird ein '?' eingefügt
                CharsetDecoder decoder = charset.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPLACE)
                        .onUnmappableCharacter(CodingErrorAction.REPLACE);

                String content = decoder.decode(ByteBuffer.wrap(bytes)).toString();

                // GSON versucht nun den (bereinigten) String zu parsen
                Map<String, Object> loadedData = gson.fromJson(content,
                        new TypeToken<Map<String, Object>>(){}.getType());

                if (loadedData != null) {
                    this.configData = loadedData;
                    success = true;
                    break;
                }
            } catch (Exception e) {
                lastError = e;
            }
        }

        if (!success) {
            System.err.println("[LangVelo] KRITISCH: Die Datei " + fileName + " konnte nicht verarbeitet werden!");
            if (lastError != null) System.err.println("[LangVelo] Fehler: " + lastError.getMessage());
        } else {
            System.out.println("[LangVelo] Konfiguration " + fileName + " erfolgreich geladen.");
        }
    }

    public void reload() {
        load();
    }
}