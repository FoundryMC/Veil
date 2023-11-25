package foundry.veil.model.graveyard.attach;

import foundry.veil.model.graveyard.update.InterpolatedSkeleton;

/**
 * Implemented by entities that utilize an Interpolated Skeleton. Provides an interface between the entity and the {@link InterpolatedEntityRenderer}
 */
public interface InterpolatedSkeletonParent {
    void setSkeleton(InterpolatedSkeleton skeleton);
    InterpolatedSkeleton getSkeleton();
}
