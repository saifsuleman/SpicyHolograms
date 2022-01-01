package io.pulsarlabs.spicyholograms.holograms;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface Hologram {
    void subscribe(Player player);

    void unsubscribe(Player player);

    void subscribeAll(Collection<Player> players);

    void unsubscribeAll(Collection<Player> players);

    void hide(Player player);

    void show(Player player);

    Location location();

    Hologram location(Location location);

    boolean inRange(Player player);

    void close();
}
