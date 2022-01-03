package io.pulsarlabs.spicyholograms.holograms;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public abstract class Hologram {
    public void subscribeAll(Collection<Player> players) {
        players.forEach(this::subscribe);
    }

    public void unsubscribeAll(Collection<Player> players) {
        players.forEach(this::unsubscribe);
    }

    public boolean inRange(Player player) {
        Location loc = location();
        if (loc.getWorld() != player.getWorld()) return false;
        return loc.distance(player.getLocation()) < (player.getClientViewDistance() * 16);
    }

    public void close() {
        this.unsubscribeAll(viewers());
    }

    public abstract void subscribe(Player player);

    public abstract void unsubscribe(Player player);

    public abstract Location location();

    public abstract Collection<Player> viewers();
}
