package de.mecrytv.languageBackend.listeners;

import de.mecrytv.languageBackend.LanguageBackend;
import de.mecrytv.languageBackend.gui.LanguageGUI;
import de.mecrytv.languageBackend.models.mariadb.LanguageNode;
import de.mecrytv.languageBackend.models.redis.LanguageModel;
import de.mecrytv.languageBackend.utils.TranslationUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.persistence.PersistentDataType;

public class LanguageListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        LanguageBackend plugin = LanguageBackend.getInstance();
        LanguageNode node = plugin.getCacheService().getNode("languages_cache");
        LanguageModel model = node.get(player.getUniqueId().toString());

        if (model == null) return;

        String rawTitle = plugin.getLanguageAPI().getTranslation(model.getLanguageCode(), "gui.title");
        if (!event.getView().title().equals(MiniMessage.miniMessage().deserialize(rawTitle))) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        String selectedLang = event.getCurrentItem().getItemMeta()
                .getPersistentDataContainer().get(LanguageGUI.LANG_KEY, PersistentDataType.STRING);

        if (selectedLang == null) return;

        if (selectedLang.equalsIgnoreCase(model.getLanguageCode())) {
            TranslationUtils.sendTranslation(player, model.getLanguageCode(), "listeners.gui_already_selected");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        model.setLanguageCode(selectedLang);
        node.set(model);

        TranslationUtils.sendTranslation(player, selectedLang, "listeners.gui_success_changed");

        player.closeInventory();
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() == null) {
            event.setCancelled(true);
        }
    }
}