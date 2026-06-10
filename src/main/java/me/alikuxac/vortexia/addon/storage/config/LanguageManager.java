// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.config;

import me.alikuxac.vortexia.addon.storage.StorageAddon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LanguageManager {

    private final StorageAddon addon;
    private FileConfiguration config;
    private File configFile;

    public LanguageManager(StorageAddon addon) {
        this.addon = addon;
        reload();
    }

    public void reload() {
        if (configFile == null) {
            configFile = new File(addon.getDataFolder(), "messages.yml");
        }
        
        if (!configFile.exists()) {
            addon.saveResource("messages.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);

        // Look for defaults in the jar
        InputStream defConfigStream = addon.getResource("messages.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            config.setDefaults(defConfig);
        }
    }

    public String getRaw(String key) {
        return config.getString(key, "Missing key: " + key);
    }

    public Component getMessage(String key) {
        String msg = getRaw(key);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }

    public String getFormatted(String key) {
        return getRaw(key).replace("&", "§");
    }

    public List<String> getStringList(String key) {
        return config.getStringList(key);
    }

    public Component parse(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            addon.getLogger().severe("Could not save messages.yml!");
        }
    }
}
