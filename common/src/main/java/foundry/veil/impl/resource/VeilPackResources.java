package foundry.veil.impl.resource;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import foundry.veil.api.resource.VeilResource;
import foundry.veil.impl.resource.tree.VeilResourceFolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NativeResource;

import static org.lwjgl.opengl.GL11C.*;

@ApiStatus.Internal
public class VeilPackResources implements NativeResource {

    private final String name;
    private final VeilResourceFolder root;
    private int texture;

    public VeilPackResources(String name) {
        this.name = name;
        this.root = new VeilResourceFolder(name);
    }

    public void add(@Nullable PackType packType, ResourceLocation loc, VeilResource<?> resource) {
        if (packType != null) {
            this.root.addResource(packType.getDirectory() + "/" + loc.getNamespace() + "/" + loc.getPath(), resource);
        } else {
            this.root.addResource(loc.getPath(), resource);
        }
    }

    public @Nullable VeilResource<?> getVeilResource(String namespace, String path) {
        VeilResourceFolder rootFolder = this.root.getFolder(namespace);
        if (rootFolder != null) {
            VeilResource<?> resource = rootFolder.getResource(path);
            if (resource != null) {
                return resource;
            }
        }

        for (PackType value : PackType.values()) {
            VeilResourceFolder packFolder = this.root.getFolder(value.getDirectory());
            if (packFolder == null) {
                continue;
            }

            VeilResourceFolder namespaceFolder = packFolder.getFolder(namespace);
            if (namespaceFolder != null) {
                VeilResource<?> rootResource = namespaceFolder.getResource(path);
                if (rootResource != null) {
                    return rootResource;
                }
            }
        }

        return null;
    }

    public void loadIcon(NativeImage image, boolean blur) {
        if (this.texture == 0) {
            this.texture = glGenTextures();
        }

        TextureUtil.prepareImage(this.texture, image.getWidth(), image.getHeight());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, blur ? GL_LINEAR : GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, blur ? GL_LINEAR : GL_NEAREST);
        image.upload(0, 0, 0, false);
    }

    public String getName() {
        return this.name;
    }

    public VeilResourceFolder getRoot() {
        return this.root;
    }

    public int getTexture() {
        return this.texture;
    }

    @Override
    public void free() {
        if (this.texture != 0) {
            glDeleteTextures(this.texture);
            this.texture = 0;
        }
    }
}
