package io.pulsarlabs.spicyholograms.holograms;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface Hologram {
    void subscribe(Player player);

    void unsubscribe(Player player);

    default void subscribeAll(Collection<Player> players) {
        players.forEach(this::subscribe);
    }

    default void unsubscribeAll(Collection<Player> players) {
        players.forEach(this::unsubscribe);
    }

    Location location();

    default boolean inRange(Player player) {
        Location loc = location();
        if (loc.getWorld() != player.getWorld()) return false;
        return loc.distance(player.getLocation()) < (player.getClientViewDistance() * 16);
    }

    void close();

    Collection<Player> getViewers();
}
