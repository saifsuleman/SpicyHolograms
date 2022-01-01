package io.pulsarlabs.spicyholograms.holograms;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.pulsarlabs.spicyholograms.SpicyHolograms;
import io.pulsarlabs.spicyholograms.storage.DataFileYML;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HologramsManager implements AutoCloseable {
    private final DataFileYML dataFile;
    private final Map<String, Hologram> holograms;
    private final BukkitRunnable runnable;
    private final ExecutorService executor;

    public HologramsManager(SpicyHolograms plugin) {
        this.dataFile = new DataFileYML("holograms.yml");
        this.holograms = new ConcurrentHashMap<>();
        this.loadHolograms();

        this.executor = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder().setNameFormat("SpicyHolograms Thread [#%d]").build());
        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Hologram hologram : holograms.values()) {
                    executor.execute(() -> {
                        List<Player> subscribers = new ArrayList<>();
                        List<Player> unsubscribers = new ArrayList<>();

                        Bukkit.getOnlinePlayers().forEach((player) -> (hologram.inRange(player) ? subscribers : unsubscribers).add(player));

                        hologram.subscribeAll(subscribers);
                        hologram.unsubscribeAll(unsubscribers);
                    });
                }
            }
        };
        this.runnable.runTaskTimerAsynchronously(plugin, 0, 2);
    }

    public Hologram createHologram(String id, Location location, List<Component> lines) {
        if (this.holograms.containsKey(id)) {
            throw new IllegalArgumentException("There already exists hologram with id: " + id + "! Remove that one first!");
        }
        Hologram hologram = new Hologram(location, lines, 0.25, true);
        this.holograms.put(id, hologram);
        hologram.save();
        return hologram;
    }

    public boolean removeHologram(String id) {
        Hologram hologram = this.holograms.remove(id);
        if (hologram != null) {
            if (hologram.isPersist()) {
                this.dataFile.getConfig().set(id, null);
                this.dataFile.save();
            }

            hologram.close();
        }
        return hologram != null;
    }

    public String removeHologram(Hologram hologram) {
        String id = getHologramId(hologram);
        if (id != null) removeHologram(id);
        return id;
    }

    public String getHologramId(Hologram hologram) {
        for (Map.Entry<String, Hologram> entry : this.holograms.entrySet()) {
            if (entry.getValue().equals(hologram)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Hologram getHologram(String id) {
        return this.holograms.getOrDefault(id, null);
    }

    public Map<String, Hologram> getHolograms() {
        return holograms;
    }

    public void loadHolograms() {
        for (String key : this.dataFile.getConfig().getKeys(false)) {
            SpicyHolograms.getInstance().getLogger().info("Loading Hologram: " + key);

            ConfigurationSection section = this.dataFile.getConfig().getConfigurationSection(key);
            if (section == null) continue;

            List<String> lines = section.getStringList("lines");
            List<Component> components = new ArrayList<>();
            for (String line : lines) {
                Component component = GsonComponentSerializer.gson().deserialize(line);
                components.add(component);
            }

            ConfigurationSection locationSection = section.getConfigurationSection("location");
            if (locationSection == null) continue;
            double x = locationSection.getDouble("x");
            double y = locationSection.getDouble("y");
            double z = locationSection.getDouble("z");
            String worldName = locationSection.getString("world");
            if (worldName == null) continue;
            World world = Bukkit.getWorld(worldName);
            Location location = new Location(world, x, y, z);

            Hologram hologram = new Hologram(location, components, 0.25, true);
            this.holograms.put(key, hologram);
        }
    }

    public void saveHologram(Hologram hologram) {
        String id = getHologramId(hologram);
        if (id == null) return;

        GsonComponentSerializer serializer = GsonComponentSerializer.gson();
        List<String> lines = new ArrayList<>();
        for (Component component : hologram.lines()) {
            lines.add(serializer.serialize(component));
        }
        Map<String, Object> location = new HashMap<>();
        location.put("x", hologram.location().getX());
        location.put("y", hologram.location().getY());
        location.put("z", hologram.location().getZ());
        location.put("world", hologram.location().getWorld().getName());

        Map<String, Object> serialized = new HashMap<>();
        serialized.put("location", location);
        serialized.put("lines", lines);

        this.dataFile.getConfig().set(id, serialized);
        this.dataFile.save();
    }

    @Override
    public void close() {
        this.runnable.cancel();

        for (Map.Entry<String, Hologram> entry : this.holograms.entrySet()) {
            SpicyHolograms.getInstance().getLogger().info("Unloading Hologram: " + entry.getKey());
            entry.getValue().close();
        }
    }
}
