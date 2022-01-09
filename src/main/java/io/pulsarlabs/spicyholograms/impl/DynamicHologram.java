package io.pulsarlabs.spicyholograms.impl;

import io.pulsarlabs.spicyholograms.Hologram;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DynamicHologram extends Hologram {
    private final Map<Player, StaticHologram> holograms;

    private Location location;
    private Function<Player, List<Component>> function;

    public DynamicHologram(Location location, Function<Player, List<Component>> function) {
        this.holograms = new ConcurrentHashMap<>();
        this.location = location;
        this.function = function;
    }

    public void subscribe(Player player) {
        StaticHologram hologram = this.holograms.computeIfAbsent(player, key -> new StaticHologram(location, lines(player), 0.25));
        hologram.subscribe(player);
    }

    public void unsubscribe(Player player) {
        StaticHologram hologram = this.holograms.remove(player);
        if (hologram != null) hologram.close();
    }

    @Override
    public Location location() {
        return this.location;
    }

    private List<Component> lines(Player player) {
        return this.function.apply(player);
    }

    public DynamicHologram location(Location location) {
        if (this.location == location) return this;
        this.location = location;

        for (StaticHologram hologram : this.holograms.values()) {
            hologram.location(this.location);
        }

        return this;
    }

    public DynamicHologram lines(Function<Player, List<Component>> function) {
        if (this.function != function) {
            this.function = function;
            update();
        }
        return this;
    }

    public void update() {
        for (Map.Entry<Player, StaticHologram> entry : this.holograms.entrySet()) {
            entry.getValue().lines(function.apply(entry.getKey())).location(this.location);
        }
    }

    @Override
    public Collection<Player> viewers() {
        return this.holograms.keySet();
    }
}
