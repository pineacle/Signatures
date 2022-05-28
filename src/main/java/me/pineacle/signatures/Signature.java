package me.pineacle.signatures;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NBTList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.var;
import me.pineacle.signatures.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public class Signature {

    private final SignaturesPlugin plugin;
    private final Player player;
    private final ItemStack toSign;
    @Setter private boolean fit;

    /**
     * Adds a signature to an item via command
     *
     * @param args signature arguments
     */
    public void sign(String args) {
        if (toSign == null || toSign.getType() == Material.AIR) {
            player.sendMessage(plugin.getLanguageLoader().get("hold-item"));
        } else {
            NBTItem item = new NBTItem(toSign, true);
            var meta = toSign.getItemMeta();

            // name tag
            if (item.hasKey("signature-item")) {

                String removeColorChar = args.replaceAll("&", String.valueOf(ChatColor.COLOR_CHAR));
                String removeHex = removeColorChar.replaceAll("#[a-fA-F0-9]{6}", "");
                String strippedLength = ChatColor.stripColor(removeHex);

                if (strippedLength.length() < plugin.getConfigHandler().getConfiguration().getInt("settings.min-length")) {
                    player.sendMessage(plugin.getLanguageLoader().get("min-length"));
                    return;
                }
                if (strippedLength.length() > plugin.getConfigHandler().getConfiguration().getInt("settings.max-length")) {
                    player.sendMessage(plugin.getLanguageLoader().get("max-length"));
                    return;
                }

                if (containsBlacklist(strippedLength)) {
                    player.sendMessage(plugin.getLanguageLoader().get("signature-contains-blacklist"));
                    return;
                }

                List<String> lore = meta.getLore();
                List<String> edited = new ArrayList<>();

                lore.clear();
                for (String line : plugin.getConfigHandler().getConfiguration().getStringList("settings.signature-item.lore").stream().map(StringUtils::format).collect(Collectors.toList())) {
                    edited.add(line.replaceAll("%signature%", StringUtils.format(args)));
                }
                meta.setLore(edited);
                toSign.setItemMeta(meta);
                player.sendMessage(plugin.getLanguageLoader().get("success-signed"));
                NBTItem test = new NBTItem(toSign, true);
                test.setString("signature-item", StringUtils.format(args));
                return;
            }

            NBTCompound compound = item.getCompound("signature-info");

            // has signatures already
            if (compound != null && meta.hasLore() && compound.getInteger("total") > 0) {

                String removeColorChar = args.replaceAll("&", String.valueOf(ChatColor.COLOR_CHAR));
                String removeHex = removeColorChar.replaceAll("#[a-fA-F0-9]{6}", "");
                String strippedLength = ChatColor.stripColor(removeHex);

                if (strippedLength.length() < plugin.getConfigHandler().getConfiguration().getInt("settings.min-length")) {
                    player.sendMessage(plugin.getLanguageLoader().get("min-length"));
                    return;
                }
                if (strippedLength.length() > plugin.getConfigHandler().getConfiguration().getInt("settings.max-length")) {
                    player.sendMessage(plugin.getLanguageLoader().get("max-length"));
                    return;
                }

                if (containsBlacklist(strippedLength)) {
                    player.sendMessage(plugin.getLanguageLoader().get("signature-contains-blacklist"));
                    return;
                }

                List<String> lore = meta.getLore();
                final int start = lore.indexOf(plugin.getConfigHandler().format("settings.format.header")); // start of signature section
                int total = start + compound.getInteger("total"); // total signatures including header
                List<String> signatures = new ArrayList<>();
                signatures.addAll(compound.getStringList("signatures")); // fetch current signatures

                lore.subList(start, (total + 1)).clear(); // clear section of signatures

                // handle maximum signatures
                if (signatures.size() + 1 > plugin.getConfigHandler().getConfiguration().getInt("settings.signature-limit")) {
                    player.sendMessage(plugin.getLanguageLoader().get("limit-reached"));
                    return;
                }

                setFit(true);

                // store signature
                signatures.add(args);

                // add header and signatures
                lore.add(plugin.getConfigHandler().format("settings.format.header"));
                for (String line : signatures) {
                    lore.add(StringUtils.format(plugin.getConfigHandler().format("settings.format.each") + line));
                }
                meta.setLore(lore);
                toSign.setItemMeta(meta);

                // add data to NBT
                NBTItem nbtItem = new NBTItem(toSign, true);
                NBTCompound comp = nbtItem.getCompound("signature-info");
                comp.setInteger("total", signatures.size());
                NBTList<String> signs = comp.getStringList("signatures");
                signs.clear();
                signs.addAll(signatures);

                player.sendMessage(plugin.getLanguageLoader().get("success-signed"));

            } else {

                String removeColorChar = args.replaceAll("&", String.valueOf(ChatColor.COLOR_CHAR));
                String removeHex = removeColorChar.replaceAll("#[a-fA-F0-9]{6}", "");
                String strippedLength = ChatColor.stripColor(removeHex);

                if (strippedLength.length() < plugin.getConfigHandler().getConfiguration().getInt("settings.min-length")) {
                    player.sendMessage(plugin.getLanguageLoader().get("min-length"));
                    return;
                }
                if (strippedLength.length() > plugin.getConfigHandler().getConfiguration().getInt("settings.max-length")) {
                    player.sendMessage(plugin.getLanguageLoader().get("max-length"));
                    return;
                }

                if (containsBlacklist(strippedLength)) {
                    player.sendMessage(plugin.getLanguageLoader().get("signature-contains-blacklist"));
                    return;
                }

                List<String> signatures = new ArrayList<>();

                setFit(true);

                signatures.add(args);

                // handle if item has lore or not / add signatures
                if (!meta.hasLore()) {
                    List<String> lore = new ArrayList<>();
                    lore.add(plugin.getConfigHandler().format("settings.format.header"));
                    for (String line : signatures) {
                        lore.add(StringUtils.format(plugin.getConfigHandler().format("settings.format.each") + line));
                    }
                    meta.setLore(lore);
                } else {
                    List<String> lore = meta.getLore();
                    lore.add(plugin.getConfigHandler().format("settings.format.header"));
                    for (String line : signatures) {
                        lore.add(StringUtils.format(plugin.getConfigHandler().format("settings.format.each") + line));
                    }
                    meta.setLore(lore);
                }
                toSign.setItemMeta(meta);

                // add data to NBT
                NBTItem nbtItem = new NBTItem(toSign, true);
                nbtItem.addCompound("signature-info");
                NBTCompound comp = nbtItem.getCompound("signature-info");
                NBTList<String> signs = comp.getStringList("signatures");
                signs.addAll(signatures);
                comp.setInteger("total", signatures.size());

                player.sendMessage(plugin.getLanguageLoader().get("success-signed"));
            }
        }
    }

    /**
     * Removes a signature
     *
     * @param line line to be removed
     */
    public void remove(int line) {
        if (toSign == null || toSign.getType() == Material.AIR) {
            player.sendMessage(plugin.getLanguageLoader().get("hold-item"));
        } else {
            NBTItem item = new NBTItem(toSign, true);
            NBTCompound compound = item.getCompound("signature-info");
            var meta = toSign.getItemMeta();

            // no signatures
            if (compound == null || !toSign.getItemMeta().hasLore())
                player.sendMessage(plugin.getLanguageLoader().get("no-signatures"));
            else {
                List<String> lore = meta.getLore(); // if it has signatures, we already know it has a lore, no need to check
                int start = lore.indexOf(plugin.getConfigHandler().format("settings.format.header")); // start of signature section
                int total = start + compound.getInteger("total"); // total signatures including header
                List<String> signatures = new ArrayList<>();
                signatures.addAll(compound.getStringList("signatures")); // fetch current signatures

                if (signatures.size() < line) {
                    player.sendMessage(plugin.getLanguageLoader().get("signature-remove-outofbounds"));
                    return;
                }

                lore.subList(start, (total + 1)).clear(); // clear section containing signatures

                signatures.remove((line - 1)); // removes line at (line - 1) so players don't start at 0

                // re-add signatures
                if (!signatures.isEmpty()) {
                    lore.add(plugin.getConfigHandler().format("settings.format.header"));
                    for (String signature : signatures) {
                        lore.add(StringUtils.format(plugin.getConfigHandler().format("settings.format.each") + signature)); // re-add signatures
                    }
                }

                meta.setLore(lore);
                toSign.setItemMeta(meta);

                // add new data to NBT
                NBTItem nbtItem = new NBTItem(toSign, true);
                NBTCompound comp = nbtItem.getCompound("signature-info");
                comp.setInteger("total", signatures.size());
                NBTList<String> signs = comp.getStringList("signatures");
                signs.clear();
                signs.addAll(signatures);

                player.sendMessage(plugin.getLanguageLoader().get("success-removed-line").replaceAll("%line%", String.valueOf(line)));

            }
        }
    }

    public boolean containsBlacklist(final String signature) {
        for (final Predicate<String> predicate : plugin.getBlacklist()) {
            if (predicate.test(ChatColor.stripColor(signature.toLowerCase()))) {
                return true;
            }
        }
        return false;
    }


}
