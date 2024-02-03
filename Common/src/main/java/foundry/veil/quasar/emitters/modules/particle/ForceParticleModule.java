package foundry.veil.quasar.emitters.modules.particle;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ParticleModule;
import org.joml.Vector3d;

public interface ForceParticleModule extends ParticleModule {

    void applyForce(QuasarParticle particle);

}
