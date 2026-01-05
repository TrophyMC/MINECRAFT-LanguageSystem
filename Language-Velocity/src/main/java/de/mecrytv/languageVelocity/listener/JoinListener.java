package de.mecrytv.languageVelocity.listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import de.mecrytv.languageVelocity.LanguageVelocity;
import de.mecrytv.languageVelocity.models.mariadb.LanguageNode;
import de.mecrytv.languageVelocity.models.redis.LanguageModel;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JoinListener {

    private final LanguageVelocity plugin = LanguageVelocity.getInstance();
    private final MinecraftChannelIdentifier channel = MinecraftChannelIdentifier.create("network", "language");

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            LanguageNode node = plugin.getServiceManager().getCacheService().getNode("languages");
            LanguageModel model = node.get(uuid.toString());

            if (model != null) {
                model = new LanguageModel(uuid, "en_US", true);
                node.set(model);
            }
            plugin.getLanguageAPI().getProfile(uuid, model.getLanguageCode());

            plugin.getLogger().info("Sprachprofil für " + player.getUsername() + " geladen: " + model.getLanguageCode());
        }).schedule();
    }

    @Subscribe
    public void onServerPostConnect(ServerPostConnectEvent event) {
        Player player = event.getPlayer();

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            LanguageNode node = plugin.getServiceManager().getCacheService().getNode("languages");
            LanguageModel model = node.get(player.getUniqueId().toString());

            if (model != null && model.isFirstJoin()) {
                sendOpenGuiMessage(player);
                model.setFirstJoin(false);
                node.set(model);
            }
        }).delay(500, TimeUnit.MILLISECONDS).schedule();
    }

    private void sendOpenGuiMessage(Player player) {
        player.getCurrentServer().ifPresent(serverConnection -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("language_gui_open");

            serverConnection.sendPluginMessage(channel, out.toByteArray());
        });
    }
}
