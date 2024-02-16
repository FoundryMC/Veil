package foundry.veil.api.quasar.emitters.module.update;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.quasar.data.module.update.TickSubEmitterModuleData;
import foundry.veil.api.quasar.emitters.module.UpdateParticleModule;
import foundry.veil.api.quasar.particle.ParticleEmitter;
import foundry.veil.api.quasar.particle.ParticleSystemManager;
import foundry.veil.api.quasar.particle.QuasarParticle;

public class TickSubEmitterModule implements UpdateParticleModule {

    private final TickSubEmitterModuleData data;

    public TickSubEmitterModule(TickSubEmitterModuleData data) {
        this.data = data;
    }

    @Override
    public void update(QuasarParticle particle) {
        if (particle.getAge() % this.data.frequency() != 0) {
            return;
        }

        ParticleSystemManager particleManager = VeilRenderSystem.renderer().getParticleManager();
        ParticleEmitter instance = particleManager.createEmitter(this.data.subEmitter());
        if (instance == null) {
            return;
        }

        instance.setPosition(particle.getPosition());
        particleManager.addParticleSystem(instance);
    }
}
