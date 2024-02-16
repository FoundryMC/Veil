package foundry.veil.api.client.graveyard.skeleton;

@FunctionalInterface
public interface SkeletonFactory<T extends InterpolatedSkeleton> {

    T create();
}
