package foundry.veil.quasar.emitters.modules.particle.update;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import org.jetbrains.annotations.NotNull;

public class BounceParticleModule implements UpdateParticleModule {
    public static final Codec<BounceParticleModule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("bounciness").forGetter(bounceParticleModule -> bounceParticleModule.bounciness)
            ).apply(instance, BounceParticleModule::new)
    );
    public float bounciness;

    public BounceParticleModule(float bounciness) {
        this.bounciness = bounciness;
    }
    @Override
    public void run(QuasarParticle particle) {
        if ((particle.isOnGround() || particle.stoppedByCollision()) && (particle.getYDelta() * particle.getYDelta() > 0.05D || particle.getXDelta() * particle.getXDelta() > 0.05D
                || particle.getZDelta() * particle.getZDelta() > 0.05D)) {
            particle.setYDelta((-particle.getYDelta() * 0.3D * bounciness * 0.4));
            particle.setXDelta(particle.getXDelta() * 0.5D * bounciness * 0.4);
            particle.setZDelta(particle.getZDelta() * 0.5D * bounciness * 0.4);
            if(particle.getXDelta() == 0) particle.setXDelta(particle.getXDelta() * -1);
            if(particle.getZDelta() == 0) particle.setZDelta(particle.getZDelta() * -1);
        }
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.BOUNCE;
    }

}
