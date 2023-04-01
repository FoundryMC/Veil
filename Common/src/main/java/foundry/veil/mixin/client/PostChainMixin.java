package foundry.veil.mixin.client;

import com.google.gson.JsonSyntaxException;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import foundry.veil.shader.RenderTargetRegistry;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(PostChain.class)
public class PostChainMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;load(Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/resources/ResourceLocation;)V"))
    private void onPostChainInit(PostChain instance, TextureManager textureManager, ResourceLocation resourceLocation) throws IOException, JsonSyntaxException {
        PostChain postChain = (PostChain) (Object) this;
        RenderTargetRegistry.modifyPostChain(postChain, resourceLocation);
        postChain.load(textureManager, resourceLocation);
    }

}
