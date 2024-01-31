package foundry.veil.quasar.util;

import foundry.veil.quasar.emitters.ParticleEmitter;

import java.util.List;

public interface EntityExtension {

    void addEmitter(ParticleEmitter emitter);

    List<ParticleEmitter> getEmitters();
}
