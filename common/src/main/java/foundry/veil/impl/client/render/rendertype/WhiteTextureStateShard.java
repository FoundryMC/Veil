package foundry.veil.impl.client.render.rendertype;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.Veil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class WhiteTextureStateShard extends RenderStateShard.EmptyTextureStateShard {

    private static final ResourceLocation TEXTURE = Veil.veilPath("special/white.png");

    public WhiteTextureStateShard() {
        super(() -> {
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            AbstractTexture texture = textureManager.getTexture(TEXTURE, null);
            if (texture == null) {
                NativeImage image = new NativeImage(1, 1, false);
                image.setPixelRGBA(0, 0, -1);
                textureManager.register(TEXTURE, new DynamicTexture(image));
            }

            RenderSystem.setShaderTexture(0, TEXTURE);
        }, () -> {
        });
    }
}
