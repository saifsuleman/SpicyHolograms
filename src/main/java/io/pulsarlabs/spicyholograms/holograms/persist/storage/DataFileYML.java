package io.pulsarlabs.spicyholograms.holograms.persist.storage;


import io.pulsarlabs.spicyholograms.SpicyHolograms;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class DataFileYML {
    private File file;
    private FileConfiguration config;

    public DataFileYML(String name) {
        if (!name.toLowerCase().endsWith(".yml")) {
            name += ".yml";
        }

        try {
            this.file = new File(SpicyHolograms.getInstance().getDataFolder() + "/" + name);
            if (!this.file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            this.config = YamlConfiguration.loadConfiguration(this.file);
            this.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        return file;
    }

    public FileConfiguration getConfig() {
        return config;
    }
}