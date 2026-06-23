package foundry.veil.api.quasar.data.module;

import com.mojang.serialization.MapCodec;

import java.util.function.Supplier;

public interface ModuleType<T extends ParticleModuleData> {
    /**
     * @return The codec for this module type data
     */
    MapCodec<T> codec();

    /**
     * @return The default values for this module when instantiated in the editor
     */
    Supplier<T> defaultValue();
}
