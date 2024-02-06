package foundry.veil.quasar.emitters.modules.particle.update;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.quasar.ParticleEmitter;
import foundry.veil.quasar.ParticleSystemManager;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.data.module.update.TickSubEmitterModuleData;
import foundry.veil.quasar.emitters.modules.particle.UpdateParticleModule;

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

    @Override
    public int getTickRate() {
        return this.data.frequency();
    }
}
