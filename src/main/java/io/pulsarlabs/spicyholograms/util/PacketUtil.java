package io.pulsarlabs.spicyholograms.util;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class PacketUtil {
    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public static WrappedChatComponent packetComponent(Component component) {
        String json = GsonComponentSerializer.gson().serialize(component);
        return WrappedChatComponent.fromJson(json);
    }

    public static void send(Player player, PacketContainer packet) {
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void sendAll(Collection<Player> players, PacketContainer packet) {
        players.forEach(player -> send(player, packet));
    }
}
