package de.mecrytv.languageBackend.commands;

import de.mecrytv.languageBackend.LanguageBackend;
import de.mecrytv.languageBackend.gui.LanguageGUI;
import de.mecrytv.languageBackend.models.mariadb.LanguageNode;
import de.mecrytv.languageBackend.models.redis.LanguageModel;
import de.mecrytv.languageBackend.utils.TranslationUtils;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LanguageCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            TranslationUtils.sendTranslation(commandSender, "en_US", "commands.only_players");
            return true;
        }

        LanguageBackend plugin = LanguageBackend.getInstance();
        LanguageNode languageNode = plugin.getCacheService().getNode("languages_cache");
        LanguageModel languageModel = languageNode.get(player.getUniqueId().toString());

        if (languageModel == null) {
            languageModel = new LanguageModel(player.getUniqueId(), "en_US", true);
            languageNode.set(languageModel);
        }

        String lang = languageModel.getLanguageCode();

        if (args.length == 0) {
            new LanguageGUI().open(player);
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.2f);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("language.reload")) {
                TranslationUtils.sendTranslation(player, lang, "commands.command_no_permission");
                return true;
            }

            plugin.getLanguageAPI().reloadAll();
            TranslationUtils.sendTranslation(player, lang, "commands.command_reload_success");
            return true;
        }

        TranslationUtils.sendTranslation(player, lang, "commands.command_usage");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("language.reload")) {
                completions.add("reload");
            }
        }
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }
}