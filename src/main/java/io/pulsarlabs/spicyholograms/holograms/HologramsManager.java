package io.pulsarlabs.spicyholograms.holograms;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.pulsarlabs.spicyholograms.SpicyHolograms;
import io.pulsarlabs.spicyholograms.holograms.impl.DynamicHologram;
import io.pulsarlabs.spicyholograms.holograms.impl.StaticHologram;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class HologramsManager implements AutoCloseable {
    private final Map<String, Hologram> holograms;
    private final BukkitRunnable runnable;
    private final ExecutorService executor;

    public HologramsManager(SpicyHolograms plugin) {
        this.holograms = new ConcurrentHashMap<>();

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

    public DynamicHologram createHologram(String id, Location location, Function<Player, List<Component>> function) {
        if (this.holograms.containsKey(id)) removeHologram(id);
        DynamicHologram hologram = new DynamicHologram(location, 0.25, function);
        this.holograms.put(id, hologram);
        return hologram;
    }

    public StaticHologram createHologram(String id, Location location, List<Component> lines) {
        if (this.holograms.containsKey(id)) removeHologram(id);
        StaticHologram hologram = new StaticHologram(location, lines, 0.25);
        this.holograms.put(id, hologram);
        return hologram;
    }

    public boolean removeHologram(String id) {
        Hologram hologram = this.holograms.remove(id);
        if (hologram != null) {
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

    @Override
    public void close() {
        this.runnable.cancel();

        for (Map.Entry<String, Hologram> entry : this.holograms.entrySet()) {
            SpicyHolograms.getInstance().getLogger().info("Unloading Hologram: " + entry.getKey());
            entry.getValue().close();
        }
    }
}
