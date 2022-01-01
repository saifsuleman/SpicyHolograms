package io.pulsarlabs.spicyholograms.holograms.impl;

import com.comphenix.protocol.events.PacketContainer;
import io.pulsarlabs.spicyholograms.holograms.Hologram;
import io.pulsarlabs.spicyholograms.holograms.HologramLine;
import io.pulsarlabs.spicyholograms.util.PacketUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DynamicHologram implements Hologram {
    private Function<Player, List<Component>> function;
    private final Set<Player> viewers;
    private final Set<Player> hiding;
    private final Map<Player, List<HologramLine>> lines;
    private final double spacing;
    private Location location;

    public DynamicHologram(Location location, double spacing, Function<Player, List<Component>> function) {
        this.function = function;
        this.location = location;
        this.spacing = spacing;
        this.lines = new ConcurrentHashMap<>();
        this.hiding = ConcurrentHashMap.newKeySet();
        this.viewers = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void subscribe(Player player) {
        if (this.hiding.contains(player)) return;

        if (this.viewers.add(player)) {
            update(player);
        }
    }

    @Override
    public void unsubscribe(Player player) {
        if (this.viewers.remove(player)) {
            for (HologramLine line : this.lines.get(player)) {
                PacketUtil.send(player, line.createDestroyPacket());
            }

            this.lines.remove(player);
        }
    }

    @Override
    public void subscribeAll(Collection<Player> players) {
        for (Player player : players) {
            subscribe(player);
        }
    }

    @Override
    public void unsubscribeAll(Collection<Player> players) {
        for (Player player : players) {
            unsubscribe(player);
        }
    }

    @Override
    public void hide(Player player) {
        if (this.hiding.add(player)) {
            this.unsubscribe(player);
        }
    }

    @Override
    public void show(Player player) {
        if (this.hiding.remove(player)) {
            this.subscribe(player);
        }
    }

    @Override
    public Location location() {
        return this.location;
    }

    @Override
    public Hologram location(Location location) {
        if (this.location == location) return this;

        this.location = location;
        for (Player player : this.viewers) {
            List<HologramLine> lines = this.lines.get(player);
            for (int i = 0; i < lines.size(); i++) {
                HologramLine line = lines.get(i);
                line.location(location.clone().subtract(0, this.spacing * i, 0));
                PacketUtil.send(player, line.createTeleportPacket());
            }
        }

        return this;
    }

    public void update(Player player) {
        List<Component> generated = this.function.apply(player);
        List<HologramLine> lines = this.lines.getOrDefault(player, new ArrayList<>());

        if (lines.size() > generated.size()) {
            for (int i = generated.size(); i < lines.size(); i++) {
                PacketContainer packet = lines.get(i).createDestroyPacket();
                PacketUtil.sendAll(this.viewers, packet);
            }

            lines.removeIf(line -> lines.indexOf(line) >= generated.size());
        }

        for (int i = 0; i < generated.size(); i++) {
            Component component = generated.get(i);

            if (i < lines.size() && lines.get(i).customName().equals(component)) {
                continue;
            }

            if (i >= lines.size()) {
                Location loc = this.location.clone().subtract(0, this.spacing * i, 0);
                HologramLine line = HologramLine.create().customName(component).location(loc);
                lines.add(line);
                PacketUtil.send(player, line.createSpawnPacket());
            }

            HologramLine line = lines.get(i).customName(component);
            PacketUtil.send(player, line.createMetadataPacket());
        }

        this.lines.put(player, lines);
    }

    public void update() {
        for (Player player : this.viewers) {
            this.update(player);
        }
    }

    public List<Component> lines(Player player) {
        return this.function.apply(player);
    }

    public DynamicHologram lines(Function<Player, List<Component>> function) {
        this.function = function;
        for (Player player : this.viewers) {
            this.update(player);
        }
        return this;
    }

    @Override
    public boolean inRange(Player player) {
        return player.getLocation().distance(location) < (player.getClientViewDistance() * 16);
    }

    @Override
    public void close() {
        this.unsubscribeAll(this.viewers);
    }
}
