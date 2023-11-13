package foundry.veil.quasar.util;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public interface ModelSetAccessor {
    LayerDefinition getLayerDefinition(ModelLayerLocation location);
}
