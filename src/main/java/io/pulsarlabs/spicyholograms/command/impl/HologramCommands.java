package io.pulsarlabs.spicyholograms.command.impl;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.annotations.specifier.Range;
import io.pulsarlabs.spicyholograms.command.SpicyCommand;
import io.pulsarlabs.spicyholograms.holograms.Hologram;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.template.TemplateResolver;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class HologramCommands extends SpicyCommand {
    @CommandMethod("holograms create <id>")
    @CommandPermission("spicyholograms.admin")
    public void createHologram(Player sender, @Argument("id") String id) {
        Location location = sender.getLocation();
        List<Component> lines = Collections.singletonList(MiniMessage.miniMessage()
                .deserialize("<rainbow>Hologram: <id>", TemplateResolver.resolving("id", id)));
        getPlugin().getHologramsManager().createHologram(id, location, lines);
        successMessage(sender, "You have successfully created a hologram named <green><id></green>!", "id", id);
    }

    @CommandMethod("holograms remove <hologram>")
    @CommandPermission("spicyholograms.admin")
    public void removeHologram(Player sender, @Argument("hologram") Hologram hologram) {
        String id = getPlugin().getHologramsManager().removeHologram(hologram);
        successMessage(sender, "You have successfully removed hologram named <green><id></green>!", "id", id);
    }

    @CommandMethod("holograms setline <hologram> <line> <text>")
    public void setLine(Player sender, @Argument("hologram") Hologram hologram, @Argument("line") @Range(min = "1") int line, @Argument("text") @Greedy String text) {
        List<Component> lines = hologram.lines();

        line--;

        if (line > lines.size()) {
            errorMessage(sender, "You have specified an invalid hologram line index! Maximum: <num>!", "num", lines.size());
            return;
        }

        Component component = MiniMessage.miniMessage().parse(text);
        if (line < lines.size()) {
            lines.set(line, component);
        } else {
            lines.add(line, component);
        }

        hologram.lines(lines);
        successMessage(sender, "You have successfully updated line <green><line></green> of hologram <green><hologram></green>!",
                "line", line + 1,
                "hologram", getPlugin().getHologramsManager().getHologramId(hologram));
    }
}
