package foundry.veil.quasar.net;

import foundry.veil.quasar.net.packets.QuasarParticlePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class DNDNetworking {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("quasar", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static int packetID = 0;

    public static void init() {
        CHANNEL.registerMessage(packetID++, QuasarParticlePacket.class, QuasarParticlePacket::encode, QuasarParticlePacket::new, QuasarParticlePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static <T> void sendToServer(T t) {
        CHANNEL.sendToServer(t);
    }

    public static <T> void sendToPlayer(ServerPlayer player, T t) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> {
            return player;
        }), t);
    }

    public static <T> void sendToPlayersInRange(ServerPlayer player, T t, double range) {
        CHANNEL.send(PacketDistributor.NEAR.with(() -> {
            return new PacketDistributor.TargetPoint(player.getX(), player.getY(), player.getZ(), range, player.level().dimension());
        }), t);
    }

    public static <T> void sendToPlayersInRange(Vec3 location, T t, double range, Level level) {
        CHANNEL.send(PacketDistributor.NEAR.with(() -> {
            return new PacketDistributor.TargetPoint(location.x(), location.y(), location.z(), range, level.dimension());
        }), t);
    }

    public static <T> void sendToAll(T t) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), t);
    }
}
