package foundry.veil.api.client.render.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

import static net.minecraft.client.renderer.RenderStateShard.*;
import static net.minecraft.client.renderer.RenderType.create;

public class RenderTypeRegistry {
    public static void init() {
    }
    private static ShaderInstance transparentTexture;
    private static final ShaderStateShard RENDERTYPE_TRANSPARENT_TEXTURE = new ShaderStateShard(() -> transparentTexture);

    public static Function<ResourceLocation, RenderType> TRANSPARENT_TEXTURE = Util.memoize(RenderTypeRegistry::createTransparentTexture);

    private static RenderType createTransparentTexture(ResourceLocation texture) {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_TRANSPARENT_TEXTURE)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(NO_OVERLAY)
                .createCompositeState(true);
        return create("transparent_texture", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, true, true, rendertype$compositestate);
    }


    public static ShaderInstance QUASAR_PARTICLE_ADDITIVE_MULTIPLY;

    public static RenderType translucentNoCull(ResourceLocation texture) {
        return RenderType.create("quasar_translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(texture, false, false)).setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER).setWriteMaskState(RenderStateShard.COLOR_WRITE).setCullState(RenderStateShard.NO_CULL).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).createCompositeState(false));
    }


}
