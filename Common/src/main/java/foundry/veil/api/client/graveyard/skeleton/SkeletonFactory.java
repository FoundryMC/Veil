package foundry.veil.api.client.graveyard.skeleton;

import foundry.veil.api.client.graveyard.skeleton.InterpolatedSkeleton;

@FunctionalInterface
public interface SkeletonFactory<T extends InterpolatedSkeleton> {

    T create();
}
