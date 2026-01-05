package de.mecrytv.languageVelocity.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import de.mecrytv.languageVelocity.utils.GeneralUtils;

public class LanguageCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (!GeneralUtils.isPlayer(source)) {
            System.out.println("[LangVelo] Only players can execute this command");
            return;
        }

        Player player = (Player) source;

        player.getCurrentServer().ifPresent(server -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("language_gui_open");

            server.sendPluginMessage(
                    MinecraftChannelIdentifier.create("network", "language"),
                    out.toByteArray()
            );
        });
    }
}
