package foundry.veil.quasar.emitters.modules.particle.update;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.Module;
import foundry.veil.quasar.emitters.modules.ModuleType;
import com.mojang.serialization.Codec;

import javax.annotation.Nonnull;

public interface UpdateModule extends Module {
    Codec<Module> DISPATCH_CODEC = UpdateModuleRegistry.MODULE_MAP_CODEC.dispatch("module", updateModule -> {
        if(updateModule.getType() == null) {
            throw new IllegalStateException("Module type is null");
        }
        return updateModule.getType();
    }, ModuleType::getCodec);
    void run(QuasarParticle particle);

    @Nonnull
    ModuleType<?> getType();

    @Override
    default Codec<Module> getDispatchCodec() {
        return DISPATCH_CODEC;
    }

}
