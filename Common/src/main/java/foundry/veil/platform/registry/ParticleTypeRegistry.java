package foundry.veil.platform.registry;

import foundry.veil.Veil;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.client.particle.QuasarParticleType;
import foundry.veil.quasar.client.particle.data.QuasarParticleData;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public class ParticleTypeRegistry {
    public static final RegistrationProvider<ParticleType<?>> PARTICLE_TYPES = RegistrationProvider.get(BuiltInRegistries.PARTICLE_TYPE, Veil.MODID);
    public static RegistryObject<ParticleType<QuasarParticleData>> QUASAR_BASE = PARTICLE_TYPES.register("quasar_base", QuasarParticleType::new);
}
