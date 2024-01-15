package foundry.veil.api.client.graveyard.skeleton;

import foundry.veil.api.client.graveyard.render.InterpolatedEntityRenderer;

/**
 * Implemented by entities that utilize an Interpolated Skeleton. Provides an interface between the entity and the {@link InterpolatedEntityRenderer}
 */
public interface InterpolatedSkeletonParent {

    InterpolatedSkeleton getSkeleton();

    void setSkeleton(InterpolatedSkeleton skeleton);

}
