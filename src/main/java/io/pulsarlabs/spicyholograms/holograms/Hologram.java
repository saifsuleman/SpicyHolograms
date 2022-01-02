package io.pulsarlabs.spicyholograms.holograms;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public abstract class Hologram {
    public abstract void subscribe(Player player);

    public abstract void unsubscribe(Player player);

    public abstract void subscribeAll(Collection<Player> players);

    public abstract void unsubscribeAll(Collection<Player> players);

    public abstract void hide(Player player);

    public abstract void show(Player player);

    public abstract Location location();

    public abstract Hologram location(Location location);

    public boolean inRange(Player player) {
        return player.getLocation().distance(location()) < (player.getClientViewDistance() * 16);
    }

    public abstract void close();
}
