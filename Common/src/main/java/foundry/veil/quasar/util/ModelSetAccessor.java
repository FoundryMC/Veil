package foundry.veil.quasar.util;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.jetbrains.annotations.Nullable;

public interface ModelSetAccessor {

    @Nullable LayerDefinition veil$getLayerDefinition(ModelLayerLocation location);
}
