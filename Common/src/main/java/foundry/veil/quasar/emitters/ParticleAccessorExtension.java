package foundry.veil.quasar.emitters;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

public interface ParticleAccessorExtension {
    Vec3 getPosition();
    Vec3 getVelocity();
    ClientLevel getClientLevel();
}
