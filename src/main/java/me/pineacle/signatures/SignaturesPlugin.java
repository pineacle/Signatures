package me.pineacle.signatures;

import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.Getter;
import me.pineacle.signatures.commands.SignCommand;
import me.pineacle.signatures.commands.UnsignCommand;
import me.pineacle.signatures.config.ConfigHandler;
import me.pineacle.signatures.config.LanguageLoader;
import me.pineacle.signatures.listeners.SignedItemListener;
import me.pineacle.signatures.utils.ItemBuilder;
import me.pineacle.signatures.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

@Getter
public final class SignaturesPlugin extends JavaPlugin {

    private SignaturesPlugin instance;
    private ConfigHandler configHandler;
    private LanguageLoader languageLoader;

    private Set<Predicate<String>> blacklist;

    @Override
    public void onEnable() {
        instance = this;
        configHandler = new ConfigHandler(this);
        languageLoader = new LanguageLoader(this, configHandler);
        blacklist = _getBlacklist();


        getCommand("sign").setExecutor(new SignCommand(this));
        getCommand("unsign").setExecutor(new UnsignCommand(this));
        Bukkit.getServer().getPluginManager().registerEvents(new SignedItemListener(this), this);
    }

    @Override
    public void onDisable() {
    }

    public ItemStack signItem() {
        ItemStack item = new ItemBuilder(Material.valueOf(getConfigHandler().get("settings.signature-item.material")))
                .name(getConfigHandler().format("settings.signature-item.name"))
                .customModelData(getConfigHandler().getConfiguration().getInt("settings.signature-item.modelData"))
                //.persistentData(new NamespacedKey(this, "signature-item"), "EMPTY")
                .build();
        NBTItem nbtItem = new NBTItem(item, true);
        nbtItem.setString("signature-item", "EMPTY");
        ItemMeta meta = item.getItemMeta();
        List<String> temp = new ArrayList<>();
        for (String line : getConfigHandler().getConfiguration().getStringList("settings.signature-item.lore").stream().map(StringUtils::format).collect(Collectors.toList())) {
            temp.add(StringUtils.format(line.replaceAll("%signature%", nbtItem.getString("signature-item"))));
        }
        meta.setLore(temp);
        item.setItemMeta(meta);
        return item;

    }

    private Set<Predicate<String>> _getBlacklist() {
        final Set<Predicate<String>> blacklist = new HashSet<>();

        getConfig().getStringList("blacklist").forEach(entry -> {
            try {
                blacklist.add(Pattern.compile(entry).asPredicate());
            } catch (final PatternSyntaxException e) {
                Bukkit.getServer().getLogger().warning("Invalid blacklist regex: " + entry);
            }
        });

        return blacklist;
    }

    public Set<Predicate<String>> getBlacklist() {
        return blacklist;
    }

}
