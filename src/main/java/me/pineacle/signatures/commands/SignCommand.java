package me.pineacle.signatures.commands;

import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.var;
import me.pineacle.signatures.Signature;
import me.pineacle.signatures.SignaturesPlugin;
import me.pineacle.signatures.utils.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public class SignCommand implements CommandExecutor {

    private final SignaturesPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ((sender instanceof Player))
            handlePlayer(sender, args);
        else
            handleConsole(sender, args);
        return false;
    }

    @SneakyThrows
    private boolean handleConsole(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /sign reload | item <player>");
            return false;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.getConfigHandler().reload();
                sender.sendMessage("Plugin has been reloaded.");
            }
            return false;
        }

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("item")) {
                UUIDFetcher fetcher = new UUIDFetcher(1);
                fetcher.fetchUUIDAsync(args[1], targetUUID -> {
                    if (targetUUID == null || Bukkit.getPlayer(targetUUID) == null) {
                        sender.sendMessage("§cPlayer is not online.");
                        return;
                    }
                    Bukkit.getPlayer(targetUUID).getInventory().addItem(plugin.signItem());
                });

                fetcher.shutdown();

                return false;
            }
        }

        return false;
    }

    @SneakyThrows
    private boolean handlePlayer(CommandSender sender, String[] args) {
        var player = (Player) sender;
        var item = player.getInventory().getItemInMainHand();

        // /sign
        if (args.length == 0) {
            if (player.hasPermission("signatures.admin")) {
                sender.sendMessage(plugin.getLanguageLoader().get("missing-args-sign-admin"));
                return false;
            } else {
                sender.sendMessage(plugin.getLanguageLoader().get("missing-args-sign-player"));
                return false;
            }
        }

        // /sign reload
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("signatures.admin")) {
                    plugin.getConfigHandler().reload();
                    plugin.getLogger().info(plugin.getConfig().getString("settings.format.header"));
                    player.sendMessage(plugin.getLanguageLoader().get("success-reload"));
                } else {
                    player.sendMessage(plugin.getLanguageLoader().get("no-permission"));
                }
                return false;
            } else if (args[0].equalsIgnoreCase("item")) {
                if (player.hasPermission("signatures.admin")) {
                    player.getInventory().addItem(plugin.signItem());
                } else {
                    player.sendMessage(plugin.getLanguageLoader().get("no-permission"));
                }
                return false;
            }
        }

        // /sign <item> | ...
        if (args.length >= 1) {

            if (args[0].equalsIgnoreCase("item")) {
                if (player.hasPermission("signatures.admin")) {
                    UUIDFetcher fetcher = new UUIDFetcher(1);
                    UUID targetUUID = fetcher.fetchUUID(args[1]);

                    if (targetUUID == null || Bukkit.getPlayer(targetUUID) == null) {
                        player.sendMessage("§cPlayer not online");
                        return false;
                    }

                    Bukkit.getPlayer(targetUUID).getInventory().addItem(plugin.signItem());
                    fetcher.shutdown();
                } else {
                    player.sendMessage(plugin.getLanguageLoader().get("no-permission"));

                }
                return false;
            }

            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage(plugin.getLanguageLoader().get("hold-item"));
                return false;
            }

            NBTItem nbtItem = new NBTItem(item, false);


            if (nbtItem.hasKey("signature-item")) {
                if (player.hasPermission("signatures.sign.item")) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < args.length; i++) {
                        sb.append(args[i] + " ");
                    }
                    new Signature(plugin, player, item).sign(sb.toString().trim());
                    return true;
                } else {
                    player.sendMessage(plugin.getLanguageLoader().get("no-permission"));
                    return false;
                }
            }

            if (player.hasPermission("signatures.sign.direct")) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < args.length; i++) {
                    sb.append(args[i] + " ");
                }
                new Signature(plugin, player, item).sign(sb.toString().trim());
            } else {
                player.sendMessage(plugin.getLanguageLoader().get("no-permission"));
            }

        }

        return false;
    }
}
