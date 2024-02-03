package foundry.veil.quasar.data.module.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.ParticleModuleSet;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.modules.particle.InitParticleModule;
import foundry.veil.quasar.util.CodecUtil;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public record InitialVelocityData(Vector3dc velocityDirection,
                                  boolean takesParentRotation,
                                  float strength) implements ParticleModuleData {

    public static final Codec<InitialVelocityData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CodecUtil.VECTOR3D_CODEC.fieldOf("direction").forGetter(InitialVelocityData::velocityDirection),
                    Codec.BOOL.fieldOf("take_parent_rotation").orElse(true).forGetter(InitialVelocityData::takesParentRotation),
                    Codec.FLOAT.fieldOf("strength").forGetter(InitialVelocityData::strength)
            ).apply(instance, InitialVelocityData::new));

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        // TODO takesParentRotation
        builder.addModule((InitParticleModule) particle -> particle.getVelocity().add(this.velocityDirection.normalize(this.strength, new Vector3d())));
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.INITIAL_VELOCITY;
    }
}
