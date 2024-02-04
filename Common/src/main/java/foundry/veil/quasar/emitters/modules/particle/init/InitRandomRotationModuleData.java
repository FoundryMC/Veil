package foundry.veil.quasar.emitters.modules.particle.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.ParticleModuleSet;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.modules.particle.InitParticleModule;
import foundry.veil.quasar.util.CodecUtil;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record InitRandomRotationModuleData(Vector3fc minDegrees, Vector3fc maxDegrees) implements ParticleModuleData {

    public static final Codec<InitRandomRotationModuleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecUtil.VECTOR3F_CODEC.fieldOf("min_degrees").forGetter(InitRandomRotationModuleData::minDegrees),
            CodecUtil.VECTOR3F_CODEC.fieldOf("max_degrees").forGetter(InitRandomRotationModuleData::maxDegrees)
    ).apply(instance, InitRandomRotationModuleData::new));

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule((InitParticleModule) particle -> {
            Vector3f rot = this.minDegrees.lerp(this.maxDegrees, particle.getRandomSource().nextFloat(), new Vector3f());
            particle.getRotation().add((float) Math.toRadians(rot.x), (float) Math.toRadians(rot.y), (float) Math.toRadians(rot.z));
        });
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.INIT_RANDOM_ROTATION;
    }
}
