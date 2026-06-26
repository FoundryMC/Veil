package foundry.veil.api.quasar.data.module;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface ModuleType<T extends ParticleModuleData> {
    /**
     * @return The codec for this module type data
     */
    MapCodec<T> codec();

    /**
     * @return The default values for this module when instantiated in the editor
     * @apiNote In 5.0.0 this will be required
     * @since 4.3.0
     */
    default Supplier<T> defaultValue() {
        return null;
    }

    /**
     * @return Whether the module is deprecated and what version it will be removed in
     * @since 4.3.0
     */
    default @Nullable DeprecationStatus deprecationStatus() {
        return null;
    }

    /**
     * Marks a module type as deprecated and scheduled for removal
     *
     * @param reason        A reason for deprecating the module
     * @param removeVersion The version the module will be removed in
     * @since 4.3.0
     */
    record DeprecationStatus(@Nullable String reason, String removeVersion) {
    }
}
