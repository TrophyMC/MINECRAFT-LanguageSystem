package de.mecrytv.languageBackend;

import de.mecrytv.LanguageAPI;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.time.Duration;

public class LanguageGUI {

    /**
     * Zeigt dem Spieler einen großen Titel statt eines Inventars.
     */
    public static void open(Player player) {
        LanguageAPI api = LanguageBackend.getInstance().getLanguageAPI();

        // Aktuelle Sprache des Spielers (Fallback de_DE)
        String lang = api.getProfile(player.getUniqueId(), "de_DE").getLanguageCode();

        // Texte aus der Sprache laden (Beispiel-Keys: join.title und join.subtitle)
        String mainTitleRaw = "<gold><bold>WILLKOMMEN</bold></gold>";
        String subTitleRaw = "<gray>Wähle deine Sprache mit <yellow>/language</yellow></gray>";

        // Titel-Einstellungen (Einfärben, Dauer)
        Title title = Title.title(
                MiniMessage.miniMessage().deserialize(mainTitleRaw),
                MiniMessage.miniMessage().deserialize(subTitleRaw),
                Title.Times.times(
                        Duration.ofMillis(500),  // Fade-In (0.5 Sek)
                        Duration.ofMillis(4000), // Anzeigezeit (4 Sek)
                        Duration.ofMillis(1000)  // Fade-Out (1 Sek)
                )
        );

        // Den Titel an den Spieler senden
        player.showTitle(title);

        // Optional: Ein Sound-Effekt beim Joinen
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }
}