package foundry.veil.api.quasar.data.module;

import com.mojang.serialization.Codec;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.QuasarParticles;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;

public interface ParticleModuleData {

    Codec<ParticleModuleData> INIT_DIRECT_CODEC = ParticleModuleTypeRegistry.INIT_MODULE_CODEC
            .dispatch("module", ParticleModuleData::getType, ModuleType::codec);
    Codec<ParticleModuleData> UPDATE_DIRECT_CODEC = ParticleModuleTypeRegistry.UPDATE_MODULE_CODEC
            .dispatch("module", ParticleModuleData::getType, ModuleType::codec);
    Codec<ParticleModuleData> RENDER_DIRECT_CODEC = ParticleModuleTypeRegistry.RENDER_MODULE_CODEC
            .dispatch("module", ParticleModuleData::getType, ModuleType::codec);

    Codec<Holder<ParticleModuleData>> INIT_CODEC = RegistryFileCodec.create(QuasarParticles.INIT_MODULES, INIT_DIRECT_CODEC);
    Codec<Holder<ParticleModuleData>> UPDATE_CODEC = RegistryFileCodec.create(QuasarParticles.UPDATE_MODULES, UPDATE_DIRECT_CODEC);
    Codec<Holder<ParticleModuleData>> RENDER_CODEC = RegistryFileCodec.create(QuasarParticles.RENDER_MODULES, RENDER_DIRECT_CODEC);

    void addModules(ParticleModuleSet.Builder builder);

    ModuleType<?> getType();
}
