package me.pineacle.signatures.commands;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.var;
import me.pineacle.signatures.Signature;
import me.pineacle.signatures.SignaturesPlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class UnsignCommand implements CommandExecutor, TabExecutor {

    private final SignaturesPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ((sender instanceof Player))
            handlePlayer(sender, args);
        else
            handleConsole(sender, args);
        return false;
    }

    private void handleConsole(CommandSender sender, String[] args) {
        sender.sendMessage("Console can't unsign items.");
    }

    @SneakyThrows
    private void handlePlayer(CommandSender sender, String[] args) {
        var player = (Player) sender;
        var item = player.getInventory().getItemInMainHand();
        if (!player.hasPermission("signatures.unsign")) {
            player.sendMessage(plugin.getLanguageLoader().get("no-permission"));
        } else {

            if (args.length == 0) {
                sender.sendMessage(plugin.getLanguageLoader().get("missing-args-unsign"));
                return;
            }

            if (args.length == 1) {

                if (StringUtils.isNumeric(args[0])) {
                    int line = Integer.parseInt(args[0]);
                    if (line <= 0) {
                        player.sendMessage("§cPlease specify a line in the signature section.");
                        return;
                    }
                    new Signature(plugin, player, item).remove(line);
                } else {
                    player.sendMessage("§cYou must enter a number");
                }
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            List<String> lines = new ArrayList<>();

            if (player.getInventory().getItemInMainHand() == null) return null;
            if (!player.getInventory().getItemInMainHand().hasItemMeta()) return null;

            var meta = player.getInventory().getItemInMainHand().getItemMeta();
            NBTItem item = new NBTItem(player.getInventory().getItemInMainHand());
            NBTCompound compound = item.getCompound("signature-info");

            // has signatures already
            if (compound != null && meta.hasLore() && compound.getInteger("total") > 0) {

                int size = compound.getStringList("signatures").size();

                for (int i = 1; i < size + 1; i++) {
                    lines.add(String.valueOf(i));
                }

            }
            return lines;
        }
        return null;
    }
}