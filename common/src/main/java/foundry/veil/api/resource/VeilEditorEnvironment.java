package foundry.veil.api.resource;

import foundry.veil.Veil;
import foundry.veil.api.client.registry.VeilResourceEditorRegistry;
import foundry.veil.api.resource.editor.ResourceFileEditor;
import net.minecraft.resources.ResourceLocation;

/**
 * An environment where files can be opened, edited, and managed.
 *
 * @author Ocelot
 * @since 1.0.0
 */
public interface VeilEditorEnvironment {

    /**
     * Opens the specified resource in the specified editor.
     *
     * @param resource The resource to open
     * @param editor   The editor to use
     * @param <T>      The type of resource opened
     */
    <T extends VeilResource<?>> void open(T resource, ResourceFileEditor.Factory<T> editor);

    /**
     * Opens the specified resource in the specified editor.
     *
     * @param resource   The resource to open
     * @param editorName The name of the editor to open
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default void open(VeilResource<?> resource, ResourceLocation editorName) {
        ResourceFileEditor.Factory factory = VeilResourceEditorRegistry.REGISTRY.get(editorName);
        if (factory == null) {
            Veil.LOGGER.error("Failed to find editor for resource: {}", resource.resourceInfo().location());
            return;
        }

        this.open(resource, factory);
    }

    /**
     * @return The resource manager instance
     */
    VeilResourceManager getResourceManager();
}
