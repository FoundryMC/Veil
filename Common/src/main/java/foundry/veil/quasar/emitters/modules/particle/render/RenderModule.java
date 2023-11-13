package foundry.veil.quasar.emitters.modules.particle.render;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.Module;
import foundry.veil.quasar.emitters.modules.ModuleType;
import com.mojang.serialization.Codec;

public interface RenderModule extends Module {
    Codec<Module> DISPATCH_CODEC = RenderModuleRegistry.MODULE_MAP_CODEC.dispatch("module", renderModule -> {
        if(renderModule.getType() == null) {
            throw new IllegalStateException("Module type is null");
        }
        return renderModule.getType();
    }, ModuleType::getCodec);
    @Override
    default Codec<Module> getDispatchCodec(){
        return DISPATCH_CODEC;
    }

    void apply(QuasarParticle particle, float partialTicks, RenderData data);
}