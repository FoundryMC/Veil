package foundry.veil.impl.resource.loader;

import foundry.veil.api.resource.VeilResource;
import foundry.veil.api.resource.VeilResourceInfo;
import foundry.veil.api.resource.VeilResourceLoader;
import foundry.veil.api.resource.VeilResourceManager;
import foundry.veil.api.resource.type.TextResource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public class TextResourceLoader implements VeilResourceLoader {

    private static final TextResource.Type[] RESOURCE_TYPES = TextResource.Type.values();

    @Override
    public boolean canLoad(PackType packType, ResourceLocation location, @Nullable Path filePath, @Nullable Path modResourcePath) {
        for (TextResource.Type type : RESOURCE_TYPES) {
            if (location.getPath().endsWith(type.getExtension())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public VeilResource<?> load(VeilResourceManager resourceManager, ResourceProvider provider, PackType packType, ResourceLocation location, @Nullable Path filePath, @Nullable Path modResourcePath) throws IOException {
        for (TextResource.Type type : RESOURCE_TYPES) {
            if (location.getPath().endsWith(type.getExtension())) {
                return new TextResource(new VeilResourceInfo(packType, location, filePath, modResourcePath, false), type, type.getLanguageDefinition());
            }
        }
        throw new IOException("Unknown text resource: " + location);
    }
}
