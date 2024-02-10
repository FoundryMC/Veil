package foundry.veil.ext;

import foundry.veil.api.quasar.particle.ParticleEmitter;

import java.util.List;

public interface EntityExtension {

    void veil$addEmitter(ParticleEmitter emitter);

    List<ParticleEmitter> veil$getEmitters();
}
