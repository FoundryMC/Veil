package foundry.veil.quasar.emitters.modules.particle.render;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.emitters.modules.ParticleModule;

public interface RenderParticleModule extends ParticleModule {

    void render(QuasarVanillaParticle particle, float partialTicks, RenderData data);
}
