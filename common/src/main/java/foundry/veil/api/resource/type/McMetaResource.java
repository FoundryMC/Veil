package foundry.veil.api.resource.type;

import foundry.veil.api.resource.VeilResource;
import foundry.veil.api.resource.VeilResourceAction;
import foundry.veil.api.resource.VeilResourceInfo;
import foundry.veil.api.resource.VeilResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

@ApiStatus.Internal
public record McMetaResource(VeilResourceInfo resourceInfo,
                             @Nullable ResourceLocation basePath,
                             ResourceMetadata metadata) implements VeilResource<McMetaResource> {

    @Override
    public List<VeilResourceAction<McMetaResource>> getActions() {
        return List.of();
    }

    @Override
    public boolean canHotReload() {
        return this.basePath != null;
    }

    @Override
    public void hotReload(VeilResourceManager resourceManager) throws IOException {
        if (this.basePath == null) {
            return;
        }

        VeilResource<?> baseResource = resourceManager.getVeilResource(this.basePath);
        if (baseResource == null) {
            return;
        }

        if (baseResource.canHotReload()) {
            baseResource.hotReload(resourceManager);
        }
    }

    @Override
    public int getIconCode() {
        return 0xECEA; // Info file icon
    }
}
