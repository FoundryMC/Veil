package foundry.veil.quasar.emitters.modules;

import com.mojang.serialization.Codec;


public interface Module {
    Codec<Module> getDispatchCodec();

    ModuleType<?> getType();


}
