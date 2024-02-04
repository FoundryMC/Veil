package foundry.veil.quasar.emitters.modules.particle;

import foundry.veil.quasar.client.particle.QuasarParticle;

public interface RenderParticleModule extends ParticleModule {

    void render(QuasarParticle particle, float partialTicks);
}
