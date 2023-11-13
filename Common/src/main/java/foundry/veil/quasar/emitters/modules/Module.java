package foundry.veil.quasar.emitters.modules;

import com.mojang.serialization.Codec;

import javax.annotation.Nonnull;

public interface Module {
    Codec<Module> getDispatchCodec();

    @Nonnull
    ModuleType<?> getType();

    void renderImGuiSettings();

}
