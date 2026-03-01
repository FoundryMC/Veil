package foundry.veil.impl.resource.loader;

import foundry.veil.api.resource.VeilResource;
import foundry.veil.api.resource.VeilResourceInfo;
import foundry.veil.api.resource.VeilResourceLoader;
import foundry.veil.api.resource.VeilResourceManager;
import foundry.veil.api.resource.type.McMetaResource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public class McMetaResourceLoader implements VeilResourceLoader {

    @Override
    public boolean canLoad(PackType packType, ResourceLocation location, @Nullable Path filePath, @Nullable Path modResourcePath) {
        return location.getPath().endsWith(".mcmeta");
    }

    @Override
    public VeilResource<?> load(VeilResourceManager resourceManager, ResourceProvider provider, PackType packType, ResourceLocation location, @Nullable Path filePath, @Nullable Path modResourcePath) throws IOException {
        ResourceLocation basePath = location.withPath(s -> s.substring(0, s.length() - 7));
        Optional<Resource> optional = provider.getResource(basePath);
        if (optional.isPresent()) {
            return new McMetaResource(new VeilResourceInfo(packType, location, filePath, modResourcePath, true), basePath, optional.get().metadata());
        }

        try (InputStream stream = provider.open(location)) {
            return new McMetaResource(new VeilResourceInfo(packType, location, filePath, modResourcePath, false), null, ResourceMetadata.fromJsonStream(stream));
        }
    }
}
