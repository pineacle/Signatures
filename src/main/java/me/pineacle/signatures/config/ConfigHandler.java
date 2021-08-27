package me.pineacle.signatures.config;

import com.google.common.io.Files;
import lombok.Getter;
import lombok.SneakyThrows;
import me.pineacle.signatures.SignaturesPlugin;
import me.pineacle.signatures.utils.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
public class ConfigHandler {

    private final SignaturesPlugin plugin;
    private FileConfiguration configuration;
    private File configFile;
    private String serverLocale;
    private String header;

    public ConfigHandler(SignaturesPlugin plugin) {
        this.plugin = plugin;
        this.configuration = plugin.getConfig();
        init();
    }

    private void init() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists())
            plugin.saveDefaultConfig();
        int configVersion = this.configuration.getInt("version");
        // if config is not up-to-date
        if (configVersion != 1) {
            String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-'config_backup.yml'").format(new Date());
            File destination = new File(plugin.getDataFolder(), "/backups/" + fileName);
            try {
                Files.copy(configFile, destination);
                configFile.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            plugin.getLogger().warning("Config is old or is missing information. Creating new file and saving backup.");
            plugin.saveDefaultConfig();
        }
        File languageFolder = new File(plugin.getDataFolder(), "languages/");
        if (!languageFolder.isDirectory()) {
            languageFolder.mkdir();
            try {
                File defaultLanguageFile = new File(plugin.getDataFolder(), "languages/en_US.yml");
                InputStream stream = plugin.getResource("en_US.yml");
                byte[] buffer = new byte[stream.available()];
                stream.read(buffer);
                OutputStream outStream = new FileOutputStream(defaultLanguageFile);
                outStream.write(buffer);
            } catch (IOException e) {
                plugin.getLogger().warning("Couldn't copy language file!");
            }
        }
        this.serverLocale = this.configuration.getString("settings.language");
        if (this.serverLocale == null)
            this.serverLocale = "en_US";

        this.header = this.configuration.getString("settings.format.header");
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }

    public void reload() {
        configuration = YamlConfiguration.loadConfiguration(configFile);
        header = configuration.getString("settings.format.header");
    }

    public String get(String path) {
        return getConfiguration().getString(path);
    }

    public String format(String path) {
        return StringUtils.format(getConfiguration().getString(path));
    }

    public String getHeader() {
        return header;
    }

    @SneakyThrows
    public void set(String path, Object input) {
        getConfiguration().set(path, input);
        getConfiguration().save(configFile);
    }

}
