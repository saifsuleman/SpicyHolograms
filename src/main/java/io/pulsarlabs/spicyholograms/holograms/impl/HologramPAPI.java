package io.pulsarlabs.spicyholograms.holograms.impl;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;

import java.util.List;
import java.util.stream.Collectors;

public class HologramPAPI extends DynamicHologram {
    private List<String> lines;

    public HologramPAPI(Location location, double spacing, List<String> lines) {
        super(location, spacing, (player) -> lines.stream()
                .map(line -> PlaceholderAPI.setPlaceholders(player, line))
                .map(line -> MiniMessage.miniMessage().parse(line))
                .collect(Collectors.toList()));
        this.lines = lines;
    }

    public void lines(List<String> lines) {
        this.lines = lines;
    }

    public List<String> lines() {
        return this.lines;
    }
}
