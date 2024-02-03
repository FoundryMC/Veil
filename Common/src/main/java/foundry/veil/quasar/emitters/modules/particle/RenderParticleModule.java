package foundry.veil.quasar.emitters.modules.particle;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ParticleModule;
import foundry.veil.quasar.emitters.modules.particle.render.RenderData;

public interface RenderParticleModule extends ParticleModule {

    void render(QuasarParticle particle, float partialTicks);
}
