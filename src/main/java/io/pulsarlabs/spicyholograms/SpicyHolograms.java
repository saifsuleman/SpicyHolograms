package io.pulsarlabs.spicyholograms;

import io.leangen.geantyref.TypeToken;
import io.pulsarlabs.spicyholograms.command.SpicyCommandManager;
import io.pulsarlabs.spicyholograms.command.impl.HologramCommands;
import io.pulsarlabs.spicyholograms.command.parser.HologramParser;
import io.pulsarlabs.spicyholograms.holograms.Hologram;
import io.pulsarlabs.spicyholograms.holograms.HologramsManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpicyHolograms extends JavaPlugin {
    private static SpicyHolograms instance;

    private HologramsManager hologramsManager;

    @Override
    public void onEnable() {
        instance = this;

        this.hologramsManager = new HologramsManager(this);

        SpicyCommandManager commandManager = new SpicyCommandManager(this);
        commandManager.getCommandManager().getParserRegistry().registerParserSupplier(
                TypeToken.get(Hologram.class), o -> new HologramParser<>()
        );
        commandManager.registerCommands(HologramCommands.class);
    }

    @Override
    public void onDisable() {
        this.hologramsManager.close();
    }

    public HologramsManager getHologramsManager() {
        return hologramsManager;
    }

    public static SpicyHolograms getInstance() {
        return instance;
    }
}
