package foundry.veil.api.quasar.data.module;

import com.mojang.serialization.Codec;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.QuasarParticles;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import org.jetbrains.annotations.ApiStatus;

public interface ParticleModuleData {

    /**
     * @since 4.3.0
     */
    Codec<ParticleModuleData> DIRECT_CODEC = ParticleModuleTypeRegistry.CODEC
            .dispatch("module", ParticleModuleData::getType, ModuleType::codec);

    /**
     * @deprecated Use {@link #DIRECT_CODEC} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    Codec<ParticleModuleData> INIT_DIRECT_CODEC = DIRECT_CODEC;
    /**
     * @deprecated Use {@link #DIRECT_CODEC} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    Codec<ParticleModuleData> UPDATE_DIRECT_CODEC = DIRECT_CODEC;
    /**
     * @deprecated Use {@link #DIRECT_CODEC} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    Codec<ParticleModuleData> RENDER_DIRECT_CODEC = DIRECT_CODEC;

    /**
     * @since 4.3.0
     */
    Codec<Holder<ParticleModuleData>> CODEC = RegistryFileCodec.create(QuasarParticles.MODULES, DIRECT_CODEC);

    /**
     * @deprecated Use {@link #CODEC} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    Codec<Holder<ParticleModuleData>> INIT_CODEC = CODEC;
    /**
     * @deprecated Use {@link #CODEC} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    Codec<Holder<ParticleModuleData>> UPDATE_CODEC = CODEC;
    /**
     * @deprecated Use {@link #CODEC} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    Codec<Holder<ParticleModuleData>> RENDER_CODEC = CODEC;

    void addModules(ParticleModuleSet.Builder builder);

    ModuleType<?> getType();
}
