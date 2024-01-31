package foundry.veil.quasar.emitters.modules.particle.update;

import com.mojang.serialization.Codec;
import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.ParticleModule;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface UpdateParticleModule extends ParticleModule {

    Codec<ParticleModule> DISPATCH_CODEC = UpdateModuleRegistry.MODULE_MAP_CODEC
            .dispatch("module", renderModule -> Objects.requireNonNull(renderModule.getType(),
                    renderModule.getClass().getName() + " cannot be serialized"), ModuleType::getCodec);

    void run(QuasarVanillaParticle particle);

    @Nullable ModuleType<?> getType();

}
