package foundry.veil.quasar.net.packets;

import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.ParticleEmitterRegistry;
import foundry.veil.quasar.emitters.ParticleSystemManager;
import foundry.veil.quasar.emitters.modules.particle.update.forces.PointForce;
import foundry.veil.quasar.emitters.modules.particle.update.forces.VortexForce;
import foundry.veil.quasar.util.EntityExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class QuasarParticlePacket {
    public int entityId;
    public String particleSystem;

    public QuasarParticlePacket(String particleSystem, int entityId) {
        this.entityId = entityId;
        this.particleSystem = particleSystem;
    }

    public QuasarParticlePacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.particleSystem = buf.readUtf(32767);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeUtf(this.particleSystem);
    }

    public static void handle(QuasarParticlePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if(ctx.get().getDirection().getReceptionSide().isClient()) {
                ClientLevel level = Minecraft.getInstance().level;
                Entity entity = level.getEntity(msg.entityId);
                if(level == null) return;
                if(entity == null) return;
                ParticleEmitter emitter = ParticleEmitterRegistry.getEmitter(ResourceLocation.tryParse(msg.particleSystem)).instance();
                if(emitter == null) return;
                emitter.setPosition(((EntityExtension)entity).getHitboxCenterPos());
                emitter.setLevel(level);
                emitter.getEmitterSettingsModule().getEmissionShapeSettings().setPosition(((EntityExtension)entity)::getHitboxCenterPos);
                emitter.getParticleData().getForces().forEach(force -> {
                    if(force instanceof PointForce pf) {
                        pf.setPoint(((EntityExtension)entity)::getHitboxCenterPos);
                    }
                    if(force instanceof VortexForce vf) {
                        vf.setVortexCenter(((EntityExtension)entity)::getHitboxCenterPos);
                    }
                });
                ParticleSystemManager.getInstance().addDelayedParticleSystem(emitter);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
