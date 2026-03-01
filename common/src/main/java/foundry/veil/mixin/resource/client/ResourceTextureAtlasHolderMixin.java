package foundry.veil.mixin.resource.client;

import foundry.veil.ext.TextureAtlasExtension;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TextureAtlasHolder.class)
public class ResourceTextureAtlasHolderMixin implements TextureAtlasExtension {

    @Shadow
    @Final
    private TextureAtlas textureAtlas;

    @Override
    public boolean veil$hasTexture(ResourceLocation location) {
        return ((TextureAtlasExtension) this.textureAtlas).veil$hasTexture(location);
    }
}
