package foundry.veil.api.client.graveyard.render.mesh;

import foundry.veil.api.client.graveyard.skeleton.InterpolatedBone;
import foundry.veil.api.client.graveyard.skeleton.InterpolatedSkeleton;
import org.jetbrains.annotations.Nullable;

public interface DynamicMesh {

    void update(@Nullable InterpolatedBone part, InterpolatedSkeleton model, int ticks, float partialTick);
}
