package io.pulsarlabs.spicyholograms.holograms.impl;

import com.comphenix.protocol.events.PacketContainer;
import io.pulsarlabs.spicyholograms.holograms.Hologram;
import io.pulsarlabs.spicyholograms.holograms.HologramLine;
import io.pulsarlabs.spicyholograms.util.PacketUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StaticHologram extends Hologram {
    private final Set<Player> viewers;
    private final Set<Player> hiding;
    private final List<HologramLine> lines;
    private final double spacing;
    private Location location;

    public StaticHologram(Location location, List<Component> lines, double spacing) {
        this.location = location;
        this.spacing = spacing;
        this.lines = new ArrayList<>();
        this.hiding = ConcurrentHashMap.newKeySet();
        this.viewers = ConcurrentHashMap.newKeySet();

        this.lines(lines);
    }

    @Override
    public void subscribe(Player player) {
        if (this.hiding.contains(player)) return;

        if (this.viewers.add(player)) {
            for (HologramLine line : this.lines) {
                PacketUtil.send(player, line.createSpawnPacket());
                PacketUtil.send(player, line.createMetadataPacket());
            }
        }
    }

    @Override
    public void unsubscribe(Player player) {
        if (this.viewers.remove(player)) {
            for (HologramLine line : this.lines) {
                PacketUtil.send(player, line.createDestroyPacket());
            }
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
    public StaticHologram location(Location location) {
        if (this.location == location) return this;

        this.location = location;
        for (int i = 0; i < this.lines.size(); i++) {
            HologramLine line = this.lines.get(i);
            line.location(location.clone().subtract(0, this.spacing * i, 0));
            PacketUtil.sendAll(this.viewers, line.createTeleportPacket());
        }

        return this;
    }

    public List<Component> lines() {
        List<Component> lines = new ArrayList<>();

        for (HologramLine line : this.lines) {
            lines.add(line.customName());
        }

        return lines;
    }

    public StaticHologram lines(List<Component> lines) {
        if (this.lines.size() > lines.size()) {
            for (int i = lines.size(); i < this.lines.size(); i++) {
                PacketContainer packet = this.lines.get(i).createDestroyPacket();
                PacketUtil.sendAll(this.viewers, packet);
            }

            this.lines.removeIf(line -> this.lines.indexOf(line) >= lines.size());
        }

        for (int i = 0; i < lines.size(); i++) {
            if (i < this.lines.size() && this.lines.get(i).customName().equals(lines.get(i))) {
                continue;
            }

            if (i >= this.lines.size()) {
                Location loc = this.location.clone().subtract(0, this.spacing * i, 0);
                HologramLine line = HologramLine.create().customName(lines.get(i)).location(loc);
                this.lines.add(line);
                PacketUtil.sendAll(this.viewers, line.createSpawnPacket());
            }

            HologramLine line = this.lines.get(i).customName(lines.get(i));
            PacketUtil.sendAll(this.viewers, line.createMetadataPacket());
        }

        return this;
    }


    @Override
    public void close() {
        this.unsubscribeAll(this.viewers);
    }
}
