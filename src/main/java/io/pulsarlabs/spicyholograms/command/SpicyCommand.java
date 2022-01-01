package io.pulsarlabs.spicyholograms.command;

import io.pulsarlabs.spicyholograms.SpicyHolograms;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.template.TemplateResolver;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SpicyCommand {
    private final SpicyHolograms plugin = SpicyHolograms.getInstance();

    protected void msg(CommandSender sender, String message) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    protected void msg(CommandSender sender, String message, Object... replacements) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize(message, TemplateResolver.resolving(replacements)));
    }

    protected void successMessage(CommandSender sender, Component component) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.2f, 0.8f);
        }

        msg(sender, "<red><bold>YOUTUBER</bold> <#029c07><bold>✔</bold><#b9e0ad><italic> <message>", "message", component);
    }

    protected void successMessage(CommandSender sender, String message, Object... replacements) {
        successMessage(sender, MiniMessage.miniMessage().deserialize(message, resolver(replacements)));
    }

    protected void successMessage(CommandSender sender, String message) {
        successMessage(sender, MiniMessage.miniMessage().parse(message));
    }

    protected void errorMessage(CommandSender sender, Component component) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.2f, 0.8f);
        }

        msg(sender, "<red><bold>YOUTUBER</bold> <#ad0011><bold>✘</bold><gray><italic> <#d93b3b><message>", "message", component);
    }

    private TemplateResolver resolver(Object... objects) {
        Map<String, String> placeholders = new HashMap<>();

        for (int i = 0; i < objects.length; i += 2) {
            String key = String.valueOf(objects[i]);
            String value = String.valueOf(objects[i + 1]);
            placeholders.put(key, value);
        }

        return TemplateResolver.pairs(placeholders);
    }

    protected void errorMessage(CommandSender sender, String message, Object... replacements) {
        errorMessage(sender, MiniMessage.miniMessage().deserialize(message, resolver(replacements)));
    }

    protected void errorMessage(CommandSender sender, String message) {
        errorMessage(sender, MiniMessage.miniMessage().parse(message));
    }

    protected void sync(Runnable runnable) {
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    protected void async(Runnable runnable) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    protected SpicyHolograms getPlugin() {
        return plugin;
    }
}