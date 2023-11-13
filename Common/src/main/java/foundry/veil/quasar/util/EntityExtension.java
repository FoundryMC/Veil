package foundry.veil.quasar.util;

import foundry.veil.quasar.emitters.ParticleEmitter;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface EntityExtension {
    void addEmitter(ParticleEmitter emitter);
    List<ParticleEmitter> getEmitters();

    Vec3 getHitboxCenterPos();
}
