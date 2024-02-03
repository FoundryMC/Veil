package foundry.veil.quasar.emitters.modules.particle.render;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.particle.RenderParticleModule;

import java.util.List;

public class TrailParticleModule implements RenderParticleModule {

    private final List<TrailSettings> settings;

    public TrailParticleModule(List<TrailSettings> settings) {
        this.settings = settings;
    }

    @Override
    public void render(QuasarParticle particle, float partialTicks) {
        particle.getRenderData().getTrails().addAll(this.settings);
    }
}
