package de.mecrytv.languageBackend.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public static ItemBuilder skull() {
        Material type;
        try {
            type = Material.valueOf("PLAYER_HEAD");
        } catch (Exception e) {
            type = Material.valueOf("SKULL_ITEM");
        }
        ItemBuilder builder = new ItemBuilder(type);
        if (type.name().equals("SKULL_ITEM")) builder.item.setDurability((short) 3);
        return builder;
    }

    public ItemBuilder texture(String base64) {
        if (base64 == null || base64.isEmpty() || !(meta instanceof SkullMeta skullMeta)) return this;

        // Fix: Name darf nicht null sein, daher nutzen wir "Skull"
        GameProfile profile = new GameProfile(UUID.randomUUID(), "Skull");
        profile.getProperties().put("textures", new Property("textures", base64));

        try {
            // Wir nutzen Reflection, um das Profil in die SkullMeta zu setzen
            java.lang.reflect.Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public ItemBuilder name(String name) {
        meta.setDisplayName(name.replace("&", "§"));
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        List<String> colored = new ArrayList<>();
        for (String s : lore) colored.add(s.replace("&", "§"));
        meta.setLore(colored);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}