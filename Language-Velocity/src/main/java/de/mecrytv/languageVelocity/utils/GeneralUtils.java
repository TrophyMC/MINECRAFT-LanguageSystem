package de.mecrytv.languageVelocity.utils;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.LanguageAPI;
import de.mecrytv.languageVelocity.LanguageVelocity;
import de.mecrytv.languageVelocity.manager.ServiceManager;
import de.mecrytv.profile.ILanguageProfile;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class GeneralUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static boolean isPlayer(CommandSource source) {
        return source instanceof Player;
    }

    public static void sendTranslated(SimpleCommand.Invocation invocation, String configKey, String... replacements) {
        String langCode = "en_US";
        if (invocation.source() instanceof Player player) {
            LanguageAPI api = LanguageVelocity.getInstance().getLanguageAPI();
            ILanguageProfile profile = api.getProfile(player.getUniqueId(), "en_US");
            langCode = profile.getLanguageCode();
        }
        sendTranslatedByUserLang(invocation, langCode, configKey, replacements);
    }

    public static void sendTranslatedByUserLang(SimpleCommand.Invocation invocation, String langCode, String configKey, String... replacements) {
        LanguageVelocity plugin = LanguageVelocity.getInstance();
        ServiceManager sm = plugin.getServiceManager();
        LanguageAPI api = plugin.getLanguageAPI();
        CommandSource source = invocation.source();

        String message = api.getTranslation(langCode, configKey);

        if (message.startsWith("§c") && !langCode.equals("en_US")) {
            String fallbackMessage = api.getTranslation("en_US", configKey);
            if (!fallbackMessage.startsWith("§c")) {
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

        source.sendMessage(
                sm.getPrefix().append(miniMessage.deserialize(message))
        );
    }
}