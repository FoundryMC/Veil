package foundry.veil.quasar.emitters.modules.particle.render;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.emitters.modules.ParticleModule;
import foundry.veil.quasar.emitters.modules.ModuleType;
import com.mojang.serialization.Codec;

public interface RenderParticleModule extends ParticleModule {
    Codec<ParticleModule> DISPATCH_CODEC = RenderModuleRegistry.MODULE_MAP_CODEC.dispatch("module", renderModule -> {
        if(renderModule.getType() == null) {
            throw new IllegalStateException("Module type is null");
        }
        return renderModule.getType();
    }, ModuleType::getCodec);
    default Codec<ParticleModule> getDispatchCodec(){
        return DISPATCH_CODEC;
    }

    void apply(QuasarVanillaParticle particle, float partialTicks, RenderData data);
}
