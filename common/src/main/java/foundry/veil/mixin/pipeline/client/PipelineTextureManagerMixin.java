package foundry.veil.mixin.pipeline.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.texture.SimpleArrayTexture;
import foundry.veil.api.client.render.texture.SimpleCubemapTexture;
import foundry.veil.api.client.render.texture.TextureTypeMetadataSection;
import foundry.veil.api.client.render.texture.VeilPreloadedTexture;
import foundry.veil.ext.TextureManagerExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(TextureManager.class)
public abstract class PipelineTextureManagerMixin implements TextureManagerExtension {

    @Shadow
    @Final
    private Map<ResourceLocation, AbstractTexture> byPath;

    @Shadow
    @Final
    private ResourceManager resourceManager;

    @Shadow
    public abstract void register(ResourceLocation path, AbstractTexture texture);

    @ModifyArg(method = "getTexture(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/AbstractTexture;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;register(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/texture/AbstractTexture;)V"), index = 1)
    public AbstractTexture wrap(AbstractTexture texture, @Local(argsOnly = true) ResourceLocation path) {
        Optional<Resource> optionalResource = this.resourceManager.getResource(path);
        if (optionalResource.isPresent()) {
            try {
                Optional<TextureTypeMetadataSection> section = optionalResource.get().metadata().getSection(TextureTypeMetadataSection.SERIALIZER);
                if (section.isPresent()) {
                    return switch (section.get().type()) {
                        case TEXTURE_2D -> texture;
                        case TEXTURE_2D_ARRAY -> new SimpleArrayTexture(path);
                        case TEXTURE_CUBE_MAP -> new SimpleCubemapTexture(path);
                    };
                }
            } catch (IOException ignored) {
            }
        }
        return texture;
    }

    @Override
    public <T extends AbstractTexture & VeilPreloadedTexture> CompletableFuture<?> veil$registerPreloadedTexture(ResourceLocation path, T texture, Executor executor) {
        if (!this.byPath.containsKey(path)) {
            this.byPath.put(path, texture);
            return texture.preload(this.resourceManager, executor).thenRunAsync(() -> this.register(path, texture), command -> Minecraft.getInstance().execute(() -> RenderSystem.recordRenderCall(command::run)));
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }
}
