package de.mecrytv.languageBackend.utils;

import de.mecrytv.LanguageAPI;
import de.mecrytv.languageBackend.LanguageBackend;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public class TranslationUtils {

    public static void sendTranslation(CommandSender sender, String langCode, String configKey, String... replacements){
        LanguageBackend plugin = LanguageBackend.getInstance();
        LanguageAPI languageAPI = plugin.getLanguageAPI();

        Audience audience = plugin.adventure().sender(sender);
        String message = languageAPI.getTranslation(langCode, configKey);

        if (!langCode.equals("en_US")){
            String fallbackMessage = languageAPI.getTranslation("en_US", configKey);
            if (message == null || message.isEmpty()) {
                message = fallbackMessage;
            }
        }

        if (replacements != null && replacements.length > 1) {
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    String target = replacements[i];
                    String value = replacements[i + 1];

                    if (target != null && value != null) {
                        message = message.replace(target, value);
                    }
                }
            }
        }

        audience.sendMessage(
                plugin.getPrefix().append(MiniMessage.miniMessage().deserialize(message))
        );
    }
}
