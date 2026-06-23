package foundry.veil.api.quasar.data.module;

import com.mojang.serialization.Codec;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.QuasarParticles;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;

public interface ParticleModuleData {

    Codec<ParticleModuleData> DIRECT_CODEC = ParticleModuleTypeRegistry.CODEC
            .dispatch("module", ParticleModuleData::getType, ModuleType::codec);

    Codec<Holder<ParticleModuleData>> CODEC = RegistryFileCodec.create(QuasarParticles.MODULES, DIRECT_CODEC);

    void addModules(ParticleModuleSet.Builder builder);

    ModuleType<?> getType();
}
