package io.pulsarlabs.spicyholograms.holograms.persist;

import io.pulsarlabs.spicyholograms.SpicyHolograms;
import io.pulsarlabs.spicyholograms.holograms.Hologram;
import io.pulsarlabs.spicyholograms.holograms.HologramsManager;
import io.pulsarlabs.spicyholograms.holograms.impl.HologramPAPI;
import io.pulsarlabs.spicyholograms.holograms.impl.StaticHologram;
import io.pulsarlabs.spicyholograms.holograms.persist.storage.DataFileYML;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PersistManager {
    private final HologramsManager hologramsManager;
    private final DataFileYML dataFile;

    public PersistManager(HologramsManager hologramsManager, String filename) {
        this.hologramsManager = hologramsManager;
        this.dataFile = new DataFileYML(filename);
    }

    public void load() {
        for (String key : this.dataFile.getConfig().getKeys(false)) {
            SpicyHolograms.getInstance().getLogger().info("Loading Hologram: " + key);

            ConfigurationSection section = this.dataFile.getConfig().getConfigurationSection(key);
            if (section == null) continue;

            boolean papi = section.getBoolean("papi");
            final List<String> lines = section.getStringList("lines");

            ConfigurationSection locationSection = section.getConfigurationSection("location");
            if (locationSection == null) continue;
            double x = locationSection.getDouble("x");
            double y = locationSection.getDouble("y");
            double z = locationSection.getDouble("z");

            String worldName = locationSection.getString("world");
            if (worldName == null) continue;
            World world = Bukkit.getWorld(worldName);
            Location location = new Location(world, x, y, z);

            if (papi) {
                hologramsManager.createHologramPAPI(key, location, lines);
            } else {
                List<Component> components = lines.stream().map(line -> GsonComponentSerializer.gson().deserialize(line)).collect(Collectors.toList());
                hologramsManager.createHologram(key, location, components);
            }
        }
    }

    public List<String> serialize(Hologram hologram) {
        if (hologram instanceof HologramPAPI) return ((HologramPAPI) hologram).lines();
        if (hologram instanceof StaticHologram) {
            return ((StaticHologram) hologram).lines().stream().map(line -> GsonComponentSerializer.gson().serialize(line)).collect(Collectors.toList());
        }

        throw new IllegalArgumentException("cannot serialize hologram of type: " + hologram.getClass().getSimpleName());
    }

    public void save(Hologram hologram) {
        String id = this.hologramsManager.getHologramId(hologram);
        if (id == null) return;

        Map<String, Object> location = new HashMap<>();
        location.put("x", hologram.location().getX());
        location.put("y", hologram.location().getY());
        location.put("z", hologram.location().getZ());
        location.put("world", hologram.location().getWorld().getName());

        Map<String, Object> serialized = new HashMap<>();
        serialized.put("papi", hologram instanceof HologramPAPI);
        serialized.put("lines", serialize(hologram));
        serialized.put("location", location);

        this.dataFile.getConfig().set(id, serialized);
        this.dataFile.save();
    }
}
