package de.mecrytv.languageBackend.cache;

import com.google.common.reflect.ClassPath;
import de.mecrytv.languageBackend.LanguageBackend;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class CacheService {
    private final Map<String, CacheNode<?>> cacheNodes = new HashMap<>();

    public void initialize() {
        try {
            ClassPath classPath = ClassPath.from(getClass().getClassLoader());
            String packageName = "de.mecrytv.languageBackend.models";

            for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
                Class<?> clazz = classInfo.load();

                if (CacheNode.class.isAssignableFrom(clazz) &&
                        !Modifier.isAbstract(clazz.getModifiers())) {

                    try {
                        CacheNode<?> node = (CacheNode<?>) clazz.getDeclaredConstructor().newInstance();
                        registerNode(node);
                    } catch (Exception e) {
                        System.out.println("Konnte CacheNode " + clazz.getSimpleName() + " nicht instanziieren: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Fehler beim Scannen der CacheNodes: " + e.getMessage());
        }
    }

    public void registerNode(CacheNode<?> node) {
        cacheNodes.put(node.nodeName, node);
        node.createTableIfNotExists();
        LanguageBackend.getInstance().getLogger().info("📦 CacheNode '" + node.nodeName + "' wurde automatisch registriert.");
    }

    public void flushAll() {
        if (cacheNodes.isEmpty()) return;
        LanguageBackend.getInstance().getLogger().info("🔄 Starte globalen Datenbank-Sync für " + cacheNodes.size() + " Nodes...");
        cacheNodes.values().forEach(CacheNode::flush);
    }

    @SuppressWarnings("unchecked")
    public <T extends CacheNode<?>> T getNode(String name) {
        return (T) cacheNodes.get(name);
    }
}