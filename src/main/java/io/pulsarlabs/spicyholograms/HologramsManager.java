package io.pulsarlabs.spicyholograms;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.pulsarlabs.spicyholograms.impl.DynamicHologram;
import io.pulsarlabs.spicyholograms.impl.StaticHologram;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class HologramsManager implements AutoCloseable {
    private final Map<UUID, Hologram> holograms;
    private final BukkitRunnable runnable;
    private final ExecutorService executor;

    public HologramsManager(Plugin plugin) {
        this.holograms = new ConcurrentHashMap<>();

        this.executor = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder().setNameFormat("SpicyHolograms Thread [#%d]").build());

        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Hologram hologram : holograms.values()) {
                    List<Player> subscribers = new ArrayList<>();
                    List<Player> unsubscribers = new ArrayList<>();

                    Bukkit.getOnlinePlayers().forEach((player) -> (hologram.inRange(player) ? subscribers : unsubscribers).add(player));

                    hologram.subscribeAll(subscribers);
                    hologram.unsubscribeAll(unsubscribers);
                }
            }
        };
        this.runnable.runTaskTimerAsynchronously(plugin, 0, 2);

        executor.execute(() -> {
            while (!runnable.isCancelled()) {
                for (Hologram hologram : holograms.values()) {
                    if (hologram instanceof DynamicHologram) {
                        if (hologram.viewers().size() == 0) continue;
                        ((DynamicHologram) hologram).update();
                    }
                }
            }
        });
    }

    public StaticHologram createHologram(Location location, List<Component> lines) {
        StaticHologram hologram = new StaticHologram(location, lines, 0.25);
        this.holograms.put(UUID.randomUUID(), hologram);
        return hologram;
    }

    public DynamicHologram createHologram(Location location, Function<Player, List<Component>> function) {
        DynamicHologram hologram = new DynamicHologram(location, function);
        this.holograms.put(UUID.randomUUID(), hologram);
        return hologram;
    }

    public boolean isHologramActive(Hologram hologram) {
        return this.holograms.containsValue(hologram);
    }

    public boolean removeHologram(Hologram hologram) {
        for (UUID uuid : this.holograms.keySet()) {
            if (this.holograms.get(uuid).equals(hologram)) {
                this.holograms.get(uuid).close();
                this.holograms.remove(uuid);
                return true;
            }
        }
        return false;
    }

    public Map<UUID, Hologram> getHolograms() {
        return holograms;
    }

    public Hologram getHologram(UUID uuid) {
        return this.holograms.getOrDefault(uuid, null);
    }

    public boolean removeHologram(UUID uuid) {
        Hologram h = getHologram(uuid);
        if (h == null) return false;
        h.close();
        this.holograms.remove(uuid);
        return true;
    }

    @Override
    public void close() {
        this.runnable.cancel();

        for (Hologram hologram : this.holograms.values()) {
            hologram.close();
        }
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
