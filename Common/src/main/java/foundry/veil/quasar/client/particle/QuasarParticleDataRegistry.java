package foundry.veil.quasar.client.particle;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;

public class QuasarParticleDataRegistry {
    private static final BiMap<ResourceLocation, QuasarParticleData> DATA_BY_ID = HashBiMap.create();

    public static void register(ResourceLocation id, QuasarParticleData data) {
        DATA_BY_ID.put(id, data);
    }

    public static QuasarParticleData getData(ResourceLocation id) {
        return DATA_BY_ID.get(id);
    }

    public static ResourceLocation getDataId(QuasarParticleData data) {
        return DATA_BY_ID.inverse().get(data);
    }

    public static void clearRegisteredData() {
        DATA_BY_ID.clear();
    }

    public static void bootstrap() {}
}
