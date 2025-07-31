package foundry.veil.api.resource;

import foundry.veil.VeilClient;
import foundry.veil.api.resource.type.McMetaResource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jetbrains.annotations.Nullable;

/**
 * Client-side resource manager implementation that supports server-sided resources on the client side.
 * <br>
 * This is mainly used by the Veil resource API to view and edit resources in mods.
 *
 * @author Ocelot, RyanH
 * @see VeilResource
 * @since 1.0.0
 */
public interface VeilResourceManager {

    /**
     * @return The resource manager instance for the client
     * @since 1.3.0
     */
    static VeilResourceManager get() {
        return VeilClient.resourceManager();
    }

    /**
     * Retrieves the correct resource manager for the specified resource.
     *
     * @param resourceInfo The info of the resource to get
     * @return The sided resource manager for that resource
     */
    default ResourceManager resources(VeilResourceInfo resourceInfo) {
        return resourceInfo.packType() == PackType.SERVER_DATA ? this.serverResources() : this.clientResources();
    }

    /**
     * @return The regular client-sided resources in the <code>assets</code> folder
     */
    ResourceManager clientResources();

    /**
     * @return The server-sided resources in the <code>data</code> folder
     */
    ResourceManager serverResources();

    /**
     * Searches for a veil resource by namespace and path.
     *
     * @param namespace The namespace to get the resource from
     * @param path      The path of the resource
     * @return The resource found or <code>null</code>
     */
    @Nullable
    VeilResource<?> getVeilResource(String namespace, String path);

    /**
     * Searches for a veil resource by location.
     *
     * @param location The location to get the resource from
     * @return The resource found or <code>null</code>
     */
    default @Nullable VeilResource<?> getVeilResource(ResourceLocation location) {
        return this.getVeilResource(location.getNamespace(), location.getPath());
    }

    /**
     * Searches for veil resource metadata by location.
     *
     * @param namespace The namespace to get the resource from
     * @param path      The path of the resource
     * @return The metadata for the resource found or <code>null</code>
     */
    default @Nullable ResourceMetadata getResourceMetadata(String namespace, String path) {
        VeilResource<?> resource = this.getVeilResource(namespace, path);
        return resource instanceof McMetaResource mcMetaResource ? mcMetaResource.metadata() : null;
    }

    /**
     * Searches for veil resource metadata by location.
     *
     * @param location The location to get the resource from
     * @return The metadata for the resource found or <code>null</code>
     */
    default @Nullable ResourceMetadata getResourceMetadata(ResourceLocation location) {
        return this.getResourceMetadata(location.getNamespace(), location.getPath());
    }
}
