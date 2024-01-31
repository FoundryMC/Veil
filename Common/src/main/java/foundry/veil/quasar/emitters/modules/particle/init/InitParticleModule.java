package foundry.veil.quasar.emitters.modules.particle.init;

import com.mojang.serialization.Codec;
import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.ParticleModule;

import java.util.Objects;

public interface InitParticleModule extends ParticleModule {

    Codec<ParticleModule> DISPATCH_CODEC = InitModuleRegistry.MODULE_MAP_CODEC
            .dispatch("module", renderModule -> Objects.requireNonNull(renderModule.getType(),
            renderModule.getClass().getName() + " cannot be serialized"), ModuleType::getCodec);

    void run(QuasarVanillaParticle particle);

    default InitParticleModule copy() {
        return this;
    }
}
