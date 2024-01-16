package foundry.veil.quasar.emitters.modules.particle.init;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class InitRandomRotationParticleModule implements InitParticleModule {
    public static final Codec<InitRandomRotationParticleModule> CODEC = RecordCodecBuilder.create(i ->
            i.group(
                    Vec3.CODEC.fieldOf("min_degrees").forGetter(InitRandomRotationParticleModule::getMinDegrees),
                    Vec3.CODEC.fieldOf("max_degrees").forGetter(InitRandomRotationParticleModule::getMaxDegrees)
            ).apply(i, InitRandomRotationParticleModule::new
           )
    );

    Vec3 minDegrees;
    Vec3 maxDegrees;

    public InitRandomRotationParticleModule(Vec3 minDegrees, Vec3 maxDegrees) {
        this.minDegrees = minDegrees;
        this.maxDegrees = maxDegrees;
    }

    @Override
    public void run(QuasarParticle particle) {
        if(particle.getAge() == 0) {
            double x = Math.random() * (this.maxDegrees.x - this.minDegrees.x) + this.minDegrees.x;
            double y = Math.random() * (this.maxDegrees.y - this.minDegrees.y) + this.minDegrees.y;
            double z = Math.random() * (this.maxDegrees.z - this.minDegrees.z) + this.minDegrees.z;
            particle.addRotation(new Vec3(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z)));
        }
    }

    public Vec3 getMinDegrees() {
        return this.minDegrees;
    }

    public Vec3 getMaxDegrees() {
        return this.maxDegrees;
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.INIT_RANDOM_ROTATION;
    }

}
