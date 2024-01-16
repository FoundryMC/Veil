package foundry.veil.quasar.emitters.modules.particle.init;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class InitRandomRotationModule implements InitModule {
    public static final Codec<InitRandomRotationModule> CODEC = RecordCodecBuilder.create(i ->
            i.group(
                    Vec3.CODEC.fieldOf("min_degrees").forGetter(InitRandomRotationModule::getMinDegrees),
                    Vec3.CODEC.fieldOf("max_degrees").forGetter(InitRandomRotationModule::getMaxDegrees)
            ).apply(i, InitRandomRotationModule::new
           )
    );
    Vec3 minDegrees;
    Vec3 maxDegrees;

    public InitRandomRotationModule(Vec3 minDegrees, Vec3 maxDegrees) {
        this.minDegrees = minDegrees;
        this.maxDegrees = maxDegrees;
    }

    @Override
    public void run(QuasarParticle particle) {
        if(particle.getAge() == 0) {
            double x = Math.random() * (maxDegrees.x - minDegrees.x) + minDegrees.x;
            double y = Math.random() * (maxDegrees.y - minDegrees.y) + minDegrees.y;
            double z = Math.random() * (maxDegrees.z - minDegrees.z) + minDegrees.z;
            particle.addRotation(new Vec3(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z)));
        }
    }

    public Vec3 getMinDegrees() {
        return minDegrees;
    }

    public Vec3 getMaxDegrees() {
        return maxDegrees;
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.INIT_RANDOM_ROTATION;
    }

}
