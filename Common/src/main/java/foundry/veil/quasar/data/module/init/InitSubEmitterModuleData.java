package foundry.veil.quasar.data.module.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.ParticleModuleSet;
import foundry.veil.quasar.data.QuasarParticles;
import foundry.veil.quasar.data.ParticleEmitterData;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.ParticleSystemManager;
import foundry.veil.quasar.emitters.modules.particle.InitParticleModule;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public record InitSubEmitterModuleData(ResourceLocation subEmitter) implements ParticleModuleData {

    public static final Codec<InitSubEmitterModuleData> CODEC = ResourceLocation.CODEC.fieldOf("subemitter").xmap(InitSubEmitterModuleData::new, InitSubEmitterModuleData::subEmitter).codec();

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule((InitParticleModule) (particle -> {
            Registry<ParticleEmitterData> registry = QuasarParticles.registryAccess().registryOrThrow(QuasarParticles.EMITTER);
            ParticleEmitterData emitter = registry.get(this.subEmitter);
            if (emitter == null) {
                return;
            }

            ParticleEmitter instance = new ParticleEmitter(particle.getLevel(), emitter);
            instance.setPosition(particle.getPosition());
            ParticleSystemManager.getInstance().addParticleSystem(instance);
        }));
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.INIT_SUB_EMITTER;
    }
}
