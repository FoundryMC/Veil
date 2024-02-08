package foundry.veil.quasar.emitters.modules.particle.render;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.particle.InitParticleModule;
import foundry.veil.quasar.fx.Trail;

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
