package de.mecrytv.languageBackend.listeners;

import de.mecrytv.languageBackend.LanguageBackend;
import de.mecrytv.languageBackend.models.mariadb.LanguageNode;
import de.mecrytv.languageBackend.models.redis.LanguageModel;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.time.Duration;

public class JoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        LanguageBackend plugin = LanguageBackend.getInstance();

        LanguageNode node = plugin.getCacheService().getNode("languages_cache");
        LanguageModel model = node.get(player.getUniqueId().toString());

        boolean isInitialJoin = false;

        if (model == null) {
            model = new LanguageModel(player.getUniqueId(), "en_US", true);
            isInitialJoin = true;
        } else if (model.isFirstJoin()) {
            isInitialJoin = true;
        }

        if (isInitialJoin) {
            String lang = model.getLanguageCode();
            MiniMessage mm = MiniMessage.miniMessage();

            String mainTitleRaw = plugin.getLanguageAPI().getTranslation(lang, "listeners.join_title");
            String subTitleRaw = plugin.getLanguageAPI().getTranslation(lang, "listeners.join_subtitle")
                    .replace("<player>", player.getName());

            Title title = Title.title(
                    mm.deserialize(mainTitleRaw),
                    mm.deserialize(subTitleRaw),
                    Title.Times.times(
                            Duration.ofMillis(500),
                            Duration.ofMillis(4000),
                            Duration.ofMillis(1000)
                    )
            );

            player.showTitle(title);

            Firework fw = player.getWorld().spawn(player.getLocation(), Firework.class);
            FireworkMeta fwm = fw.getFireworkMeta();

            fwm.addEffect(FireworkEffect.builder()
                    .withColor(Color.ORANGE, Color.YELLOW)
                    .withFade(Color.WHITE)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .trail(true)
                    .flicker(true)
                    .build());

            fwm.setPower(1);
            fw.setFireworkMeta(fwm);

            model.setFirstJoin(false);
            node.set(model);
        }
    }
}