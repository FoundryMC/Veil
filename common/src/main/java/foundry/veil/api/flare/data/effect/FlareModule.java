package foundry.veil.api.flare.data.effect;

import com.mojang.serialization.Codec;

import java.util.Map;

/**
 * @since 2.5.0
 */
public final class FlareModule {
    
    public static final Codec<FlareModule> CODEC = Codec.unboundedMap(Codec.STRING, FlareSubModule.CODEC)
            .xmap(FlareModule::new, FlareModule::subModules)
            .fieldOf("subModules")
            .codec();
    private final Map<String, FlareSubModule> subModules;
    
    
    public FlareModule(Map<String, FlareSubModule> subModules) {
        this.subModules = Map.copyOf(subModules);
    }
    
    public FlareSubModule getSubModule(String name) {
        return this.subModules.get(name);
    }
    
    public Map<String, FlareSubModule> subModules() {
        return subModules;
    }
    
}
