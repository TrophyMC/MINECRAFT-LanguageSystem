package de.mecrytv.languageVelocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.languageVelocity.LanguageVelocity;
import de.mecrytv.languageVelocity.utils.GeneralUtils;
import de.mecrytv.profile.ILanguageProfile;

public class LangReloadCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (!GeneralUtils.isPlayer(source)) {
            System.out.println("[LangVelo] Only players can execute this command");
            return;
        }

        Player player = (Player) source;
        LanguageVelocity plugin = LanguageVelocity.getInstance();

        ILanguageProfile profile = plugin.getLanguageAPI().getProfile(player.getUniqueId(), "en_US");
        String userLangCode = profile.getLanguageCode();

        if (!player.hasPermission("language.velocity.reload")) {
            GeneralUtils.sendTranslatedByUserLang(invocation, userLangCode, "commands.langreload.nopermission");
            return;
        }

        plugin.getLanguageAPI().reloadAll();
        plugin.getServiceManager().sendGlobalReloadSignal();

        GeneralUtils.sendTranslatedByUserLang(invocation, userLangCode, "commands.langreload.success");
    }
}
