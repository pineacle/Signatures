package me.pineacle.signatures.config;

import me.pineacle.signatures.SignaturesPlugin;
import me.pineacle.signatures.utils.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;

public class LanguageLoader {

    private final SignaturesPlugin plugin;
    private final ConfigHandler configManager;

    private final HashMap<String, String> translationMap;

    public LanguageLoader(SignaturesPlugin plugin, ConfigHandler config) {
        this.plugin = plugin;
        this.configManager = config;
        this.translationMap = new HashMap<>();
        populateTranslationMap();
    }

    public void populateTranslationMap() {
        if (!this.configManager.getServerLocale().equals("en_US")) {
            File langFile = new File(this.plugin.getDataFolder(), "languages/" + this.configManager.getServerLocale() + ".yml");
            if (!langFile.exists()) {
                this.plugin.getLogger().warning("Defaulting language to en_US. Couldn't find the language file '" + this.configManager.getServerLocale() + ".yml'");
            } else {
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(langFile);
                for (String s : yamlConfiguration.getKeys(false)) {
                    this.translationMap.put(s, yamlConfiguration.getString(s));
                }
                this.plugin.getLogger().warning("Found language file '" + this.configManager.getServerLocale() + ".yml'");
            }
        } else {
            File langFile = new File(this.plugin.getDataFolder(), "languages/en_US.yml");
            if (!langFile.exists())
                try {
                    File defaultLanguageFile = new File(plugin.getDataFolder(), "languages/en_US.yml");
                    InputStream stream = plugin.getResource("en_US.yml");
                    byte[] buffer = new byte[stream.available()];
                    stream.read(buffer);
                    OutputStream outStream = new FileOutputStream(defaultLanguageFile);
                    outStream.write(buffer);
                } catch (IOException e) {
                    this.plugin.getLogger().warning("Couldn't copy language file!");
                }
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(langFile);
            for (String s : yamlConfiguration.getKeys(false)) {
                this.translationMap.put(s, yamlConfiguration.getString(s));
            }
            this.plugin.getLogger().warning("Found language file '" + this.configManager.getServerLocale() + ".yml'");
        }
    }

    public void reload() {
        getTranslationMap().clear();
        populateTranslationMap();
    }

    public HashMap<String, String> getTranslationMap() {
        return translationMap;
    }

    public String get(String path) {
        return StringUtils.format(this.translationMap.get(path));
    }

}
