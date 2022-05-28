package me.pineacle.signatures.commands;

import de.tr7zw.nbtapi.NBTItem;
import lombok.RequiredArgsConstructor;
import lombok.var;
import me.pineacle.signatures.Signature;
import me.pineacle.signatures.SignaturesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class SignCommand implements CommandExecutor {

    private final SignaturesPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player)
            handlePlayer(sender, args);
        else
            handleConsole(sender, args);
        return false;
    }

    private void handleConsole(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getLanguageLoader().get("missing-args-sign-admin"));
            return;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.getConfigHandler().reload();
                sender.sendMessage("Plugin has been reloaded.");
            }
            return;
        }

        if (args[0].equalsIgnoreCase("item")) {
            if (args[1] == null) sender.sendMessage(plugin.getLanguageLoader().get("missing-args-sign-admin"));
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.getLanguageLoader().get("player-offline"));
                return;
            }
            target.getInventory().addItem(plugin.signItem());
            return;
        }

        sender.sendMessage("§cConsole cannot sign items.");

    }


    private void handlePlayer(CommandSender sender, String[] args) {
        var player = (Player) sender;
        var item = player.getInventory().getItemInMainHand();

        if (args.length == 0) {
            if (player.hasPermission("signatures.admin")) {
                sender.sendMessage(plugin.getLanguageLoader().get("missing-args-sign-admin"));
                return;
            } else {
                sender.sendMessage(plugin.getLanguageLoader().get("missing-args-sign-player"));
                return;
            }
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("signatures.admin")) {
                    plugin.reloadConfig();
                    plugin.getLanguageLoader().reload();
                    player.sendMessage(plugin.getLanguageLoader().get("success-reload"));
                } else {
                    player.sendMessage(plugin.getLanguageLoader().get("no-permission"));
                }
                return;
            } else if (args[0].equalsIgnoreCase("item")) {
                if (player.hasPermission("signatures.admin")) {
                    player.getInventory().addItem(plugin.signItem());
                } else {
                    player.sendMessage(plugin.getLanguageLoader().get("no-permission"));
                }
                return;
            }
        }

        if (args[0].equalsIgnoreCase("item")) {
            if (player.hasPermission("signatures.admin")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(plugin.getLanguageLoader().get("player-offline"));
                    return;
                }

                target.getInventory().addItem(plugin.signItem());
            } else {
                player.sendMessage(plugin.getLanguageLoader().get("no-permission"));

            }
            return;
        }

        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(plugin.getLanguageLoader().get("hold-item"));
            return;
        }

        NBTItem nbtItem = new NBTItem(item, false);


        if (nbtItem.hasKey("signature-item")) {
            if (player.hasPermission("signatures.sign.item")) {
                StringBuilder sb = new StringBuilder();
                for (String arg : args) {
                    sb.append(arg).append(" ");
                }
                new Signature(plugin, player, item).sign(sb.toString().trim());
                return;
            } else {
                player.sendMessage(plugin.getLanguageLoader().get("no-permission"));
                return;
            }
        }


        if (plugin.getConfig().getBoolean("settings.direct-sign") && player.hasPermission("signatures.sign")) {
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg).append(" ");
            }
            new Signature(plugin, player, item).sign(sb.toString().trim());
        } else {
            if (player.hasPermission("signatures.sign.direct")) {
                StringBuilder sb = new StringBuilder();
                for (String arg : args) {
                    sb.append(arg).append(" ");
                }
                new Signature(plugin, player, item).sign(sb.toString().trim());
            } else {
                player.sendMessage(plugin.getLanguageLoader().get("no-permission"));
            }
        }
    }
}
