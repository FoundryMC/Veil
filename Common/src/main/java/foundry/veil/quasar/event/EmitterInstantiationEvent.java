package foundry.veil.quasar.event;

import foundry.veil.quasar.emitters.ParticleEmitter;
import net.minecraftforge.eventbus.api.Event;

public class EmitterInstantiationEvent extends Event {
    private ParticleEmitter emitter;

    public EmitterInstantiationEvent(ParticleEmitter emitter) {
        this.emitter = emitter;
    }

    public ParticleEmitter getEmitter() {
        return emitter;
    }

    public void setEmitter(ParticleEmitter emitter) {
        this.emitter = emitter;
    }
}
