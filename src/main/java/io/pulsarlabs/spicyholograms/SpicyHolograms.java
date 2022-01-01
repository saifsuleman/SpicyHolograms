package io.pulsarlabs.spicyholograms;

import io.pulsarlabs.spicyholograms.holograms.HologramsManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpicyHolograms extends JavaPlugin {
    private static SpicyHolograms instance;

    private HologramsManager hologramsManager;

    @Override
    public void onEnable() {
        instance = this;

        this.hologramsManager = new HologramsManager(this);
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
