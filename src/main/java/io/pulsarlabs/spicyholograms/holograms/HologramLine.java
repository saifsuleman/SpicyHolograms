package io.pulsarlabs.spicyholograms.holograms;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import io.pulsarlabs.spicyholograms.util.PacketUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HologramLine {
    private static final AtomicInteger ENTITY_ID_GENERATOR = new AtomicInteger(-1);

    private static final WrappedDataWatcher.WrappedDataWatcherObject FLAGS = new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class));
    private static final WrappedDataWatcher.WrappedDataWatcherObject CUSTOM_NAME = new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true));
    private static final WrappedDataWatcher.WrappedDataWatcherObject CUSTOM_NAME_VISIBLE = new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class));
    private static final WrappedDataWatcher.WrappedDataWatcherObject NO_GRAVITY = new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class));
    private static final WrappedDataWatcher.WrappedDataWatcherObject ARMOR_STAND_FLAGS = new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class));

    private final int id;
    private final UUID uuid;
    private Location location;
    private Component customName;

    private HologramLine() {
        this.id = ENTITY_ID_GENERATOR.getAndDecrement();
        this.uuid = UUID.randomUUID();
    }

    public static HologramLine create() {
        return new HologramLine();
    }

    public Component customName() {
        return customName;
    }

    public HologramLine customName(Component customName) {
        this.customName = customName;
        return this;
    }

    public Location location() {
        return this.location;
    }

    public HologramLine location(Location location) {
        this.location = location;
        return this;
    }

    public PacketContainer createSpawnPacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        packet.getIntegers().write(0, this.id);
        packet.getUUIDs().write(0, this.uuid);
        packet.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
        packet.getDoubles().write(0, this.location.getX());
        packet.getDoubles().write(1, this.location.getY());
        packet.getDoubles().write(2, this.location.getZ());
        packet.getIntegers().write(4, (int) (this.location.getPitch() * 256.0F / 360.0F));
        packet.getIntegers().write(5, (int) (this.location.getYaw() * 256.0F / 360.0F));
        return packet;
    }

    public PacketContainer createMetadataPacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packet.getModifier().writeDefaults();
        packet.getIntegers().write(0, this.id);
        packet.getWatchableCollectionModifier().write(0, getWatchableObjects());
        return packet;
    }

    public List<WrappedWatchableObject> getWatchableObjects() {
        List<WrappedWatchableObject> list = new ArrayList<>();
        list.add(new WrappedWatchableObject(FLAGS, (byte) 0x20));
        list.add(new WrappedWatchableObject(CUSTOM_NAME, Optional.of(PacketUtil.packetComponent(this.customName).getHandle())));
        list.add(new WrappedWatchableObject(CUSTOM_NAME_VISIBLE, !isComponentBlank(this.customName)));
        list.add(new WrappedWatchableObject(NO_GRAVITY, true));
        list.add(new WrappedWatchableObject(ARMOR_STAND_FLAGS, (byte) (0x01 | 0x08 | 0x10)));
        return list;
    }

    public PacketContainer createTeleportPacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
        packet.getModifier().writeDefaults();
        packet.getIntegers().write(0, this.id);
        packet.getDoubles().write(0, this.location.getX());
        packet.getDoubles().write(1, this.location.getY());
        packet.getDoubles().write(2, this.location.getZ());
        packet.getBytes().write(0, (byte) (this.location.getYaw() * 256.0F / 360.0F));
        packet.getBytes().write(1, (byte) (this.location.getPitch() * 256.0F / 360.0F));
        return packet;
    }

    public PacketContainer createDestroyPacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packet.getModifier().writeDefaults();
        packet.getIntLists().write(0, Collections.singletonList(this.id));
        return packet;
    }

    public int getId() {
        return id;
    }

    private boolean isComponentBlank(Component component) {
        return component.equals(MiniMessage.miniMessage().parse(""));
    }
}