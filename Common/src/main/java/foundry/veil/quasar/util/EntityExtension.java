package foundry.veil.quasar.util;

import foundry.veil.quasar.ParticleEmitter;

import java.util.List;

public interface EntityExtension {

    void veil$addEmitter(ParticleEmitter emitter);

    List<ParticleEmitter> veil$getEmitters();
}
