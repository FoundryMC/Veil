package foundry.veil.event;

/**
 * Fired when Minecraft frees all native resources on the client.
 *
 * @author Ocelot
 */
@FunctionalInterface
public interface FreeNativeResourcesEvent {

    /**
     * Called after all Minecraft native resources have been freed, but before the executors have been shut down.
     */
    void onFree();
}
