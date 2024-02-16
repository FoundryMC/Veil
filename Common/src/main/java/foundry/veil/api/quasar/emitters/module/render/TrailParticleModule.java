package foundry.veil.api.quasar.emitters.module.render;

import foundry.veil.api.quasar.particle.QuasarParticle;
import foundry.veil.api.quasar.emitters.module.InitParticleModule;
import foundry.veil.api.quasar.fx.Trail;

import java.util.List;

public class TrailParticleModule implements InitParticleModule {

    private final List<TrailSettings> settings;

    public TrailParticleModule(List<TrailSettings> settings) {
        this.settings = settings;
    }

    @Override
    public void init(QuasarParticle particle) {
        List<Trail> trails = particle.getRenderData().getTrails();
        for (TrailSettings setting : this.settings) {
            trails.add(new Trail(setting));
        }
    }
}
