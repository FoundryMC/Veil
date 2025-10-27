package foundry.veil.api.flare.data.effect;

import com.mojang.serialization.Codec;

import java.util.Collections;
import java.util.Map;

/**
 * @since 2.5.0
 */
public record FlareModule(Map<String, FlareSubModule> subModules) {

    public static final Codec<FlareModule> CODEC = Codec.unboundedMap(Codec.STRING, FlareSubModule.CODEC)
            .xmap(FlareModule::new, FlareModule::subModules)
            .fieldOf("subModules")
            .codec();

    public FlareModule(Map<String, FlareSubModule> subModules) {
        this.subModules = Collections.unmodifiableMap(subModules);
    }

    public FlareSubModule getSubModule(String name) {
        return this.subModules.get(name);
    }

}
