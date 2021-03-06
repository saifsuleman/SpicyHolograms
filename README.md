# SpicyHolograms

## Building
```bash
git clone https://github.com/saifsuleman/SpicyHolograms.git
cd SpicyHolograms/
mvn clean install
```

## Maven

```xml
<dependency>
    <groupId>io.pulsarlabs</groupId>
    <artifactId>SpicyHolograms</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

## Gradle

```kotlin
compileOnly("io.pulsarlabs:SpicyHolograms:1.0.0")
```

## API Usage

```java

import io.pulsarlabs.spicyholograms.HologramsManager;
import io.pulsarlabs.spicyholograms.impl.DynamicHologram;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.template.TemplateResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class MyPlugin extends JavaPlugin {
    private HologramsManager hologramsManager;
    
    @Override
    public void onEnable() {
        this.hologramsManager = new HologramsManager(this);

        List<Component> lines = Arrays.asList(
                MiniMessage.miniMessage().parse("<rainbow>My Hologram!"),
                MiniMessage.miniMessage().parse("<rainbow>This is great!"),
                MiniMessage.miniMessage().parse("<gradient:white:blue>Oh yeah this is awesome!")
        );
        Location location = new Location(Bukkit.getWorld("world"), 270, 80, 5);
        hologramsManager.createHologram(location, lines);

        DynamicHologram dynamicHologram = hologramsManager.createHologram(new Location(Bukkit.getWorld("world"), 270, 80, 10), player -> {
                    String name = player.getName();
                    boolean op = player.isOp();
                    return Arrays.asList(
                            MiniMessage.miniMessage().parse("<rainbow>This is a dynamic hologram!"),
                            MiniMessage.miniMessage().deserialize("<rainbow>Hello <player>", TemplateResolver.resolving("player", name)),
                            MiniMessage.miniMessage().parse(op ? "<rainbow>You are opped!" : "<rainbow>You are not opped!")
                    );
                }
        );
    }
    
    @Override
    public void onDisable() {
        this.hologramsManager.close();
    }
}
```