package foundry.veil.quasar.emitters.modules.particle.render;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;


/*
 This is exactly the same as {@link RotationOverVelocityModule}, the name is just to make it more clear what it does.
 */
public class RotationOverLifetimeModule implements RenderModule {
    BiFunction<QuasarParticle, Float, Vec3> rotationFunction;

    public RotationOverLifetimeModule(BiFunction<QuasarParticle, Float, Vec3> rotationFunction) {
        this.rotationFunction = rotationFunction;
    }
    @Override
    public void apply(QuasarParticle particle, float partialTicks, RenderData data) {
        Vec3 rotation = rotationFunction.apply(particle, partialTicks);
        data.vectorToRotation(rotation);
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return null;
    }

}
