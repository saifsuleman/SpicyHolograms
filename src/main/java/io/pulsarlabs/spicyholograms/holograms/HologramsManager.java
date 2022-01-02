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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class HologramsManager implements AutoCloseable {
    private final Set<Hologram> holograms;
    private final BukkitRunnable runnable;
    private final ExecutorService executor;

    public HologramsManager(SpicyHolograms plugin) {
        this.holograms = ConcurrentHashMap.newKeySet();

        this.executor = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder().setNameFormat("SpicyHolograms Thread [#%d]").build());

        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Hologram hologram : holograms) {
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

        executor.execute(() -> {
            while (!runnable.isCancelled()) {
                for (Hologram hologram : holograms) {
                    if (hologram instanceof DynamicHologram) {
                        if (hologram.getViewers().size() == 0) continue;
                        ((DynamicHologram) hologram).update();
                    }
                }
            }
        });
    }

    public StaticHologram createHologram(Location location, List<Component> lines) {
        StaticHologram hologram = new StaticHologram(location, lines, 0.25);
        this.holograms.add(hologram);
        return hologram;
    }

    public DynamicHologram createHologram(Location location, Function<Player, List<Component>> function) {
        DynamicHologram hologram = new DynamicHologram(location, function);
        this.holograms.add(hologram);
        return hologram;
    }

    public boolean isHologramActive(Hologram hologram) {
        return this.holograms.contains(hologram);
    }

    public boolean removeHologram(Hologram hologram) {
        hologram.close();
        return this.holograms.remove(hologram);
    }

    public Set<Hologram> getHolograms() {
        return holograms;
    }

    @Override
    public void close() {
        this.runnable.cancel();

        for (Hologram hologram : this.holograms) {
            hologram.close();
        }
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
