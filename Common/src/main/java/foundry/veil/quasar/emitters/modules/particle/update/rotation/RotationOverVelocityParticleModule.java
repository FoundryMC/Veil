package foundry.veil.quasar.emitters.modules.particle.update.rotation;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateParticleModule;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;

import java.util.function.Function;

public class RotationOverVelocityParticleModule implements UpdateParticleModule {

    private final Function<QuasarVanillaParticle, Vector3dc> rotationFunction;

    public RotationOverVelocityParticleModule(Function<QuasarVanillaParticle, Vector3dc> rotationFunction) {
        this.rotationFunction = rotationFunction;
    }

    @Override
    public void run(QuasarVanillaParticle particle) {
        particle.vectorToRotation(this.rotationFunction.apply(particle));
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return null;
    }

}
