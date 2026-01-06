package de.mecrytv.languageBackend.gui;

import de.mecrytv.languageBackend.LanguageBackend;
import de.mecrytv.languageBackend.enums.LanguageHeadIDs;
import de.mecrytv.languageBackend.models.mariadb.LanguageNode;
import de.mecrytv.languageBackend.models.redis.LanguageModel;
import de.mecrytv.languageBackend.utils.ItemBuilder;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LanguageGUI {

    public static final NamespacedKey LANG_KEY = new NamespacedKey("languagebackend", "code");

    public void open(Player player) {
        LanguageBackend plugin = LanguageBackend.getInstance();
        MiniMessage mm = MiniMessage.miniMessage();
        HeadDatabaseAPI hdb = new HeadDatabaseAPI();

        LanguageNode node = plugin.getCacheService().getNode("languages_cache");
        LanguageModel playerModel = node.get(player.getUniqueId().toString());
        String viewerLang = (playerModel != null) ? playerModel.getLanguageCode() : "en_US";

        String rawTitle = plugin.getLanguageAPI().getTranslation(viewerLang, "gui.title");
        Inventory gui = Bukkit.createInventory(null, 27, mm.deserialize(rawTitle));

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(Component.empty());
            fillerMeta.addItemFlags(ItemFlag.values());
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) gui.setItem(i, filler);

        Set<String> loadedLangs = plugin.getLanguageAPI().getConfigs().keySet();
        int slot = 10;

        for (String langCode : loadedLangs) {
            String hdbId = LanguageHeadIDs.getHdbIdByCode(langCode);
            if (hdbId == null) continue;

            ItemStack head = hdb.getItemHead(hdbId);
            if (head == null) continue;

            boolean isActive = langCode.equalsIgnoreCase(viewerLang);

            String name = plugin.getLanguageAPI().getTranslation(viewerLang, "gui.lang_name_" + langCode);
            String desc = plugin.getLanguageAPI().getTranslation(viewerLang, "gui.lang_desc_" + langCode);
            String status = isActive ?
                    plugin.getLanguageAPI().getTranslation(viewerLang, "gui.status_active") :
                    plugin.getLanguageAPI().getTranslation(viewerLang, "gui.status_selectable");

            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize(desc));
            lore.add(Component.empty());
            lore.add(mm.deserialize(status));

            ItemMeta meta = head.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(LANG_KEY, PersistentDataType.STRING, langCode);
                meta.addItemFlags(ItemFlag.values());
                head.setItemMeta(meta);
            }

            if (isActive) head.addUnsafeEnchantment(Enchantment.LURE, 1);

            gui.setItem(slot, ItemBuilder.modify(head, mm.deserialize(name), lore));

            slot++;
            if (slot == 17) break;
        }

        player.openInventory(gui);
    }
}