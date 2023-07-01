package foundry.veil.render.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.Veil;
import foundry.veil.postprocessing.PostProcessor;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

import static net.minecraft.client.renderer.RenderStateShard.*;
import static net.minecraft.client.renderer.RenderType.create;

public class RenderTypeRegistry {
    //private static final RenderType CUTOUT = RenderType.create("cutout", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 131072, true, false, RenderType.CompositeState.builder().setOutputState(RenderStateShardRegistry.VEIL_CUSTOM).setLightmapState(LIGHTMAP).setShaderState(RENDERTYPE_CUTOUT_SHADER).setTextureState(BLOCK_SHEET).createCompositeState(true));

    public static void init(){
//        RenderTargetRegistry.register("veil:custom", Pair.of(Minecraft.getInstance().getMainRenderTarget().width, Minecraft.getInstance().getMainRenderTarget().height), true);
//        ItemBlockRenderTypes.TYPE_BY_BLOCK.remove(Blocks.TORCH);
//        ItemBlockRenderTypes.TYPE_BY_BLOCK.remove(Blocks.WALL_TORCH);
//        ItemBlockRenderTypes.TYPE_BY_BLOCK.put(Blocks.TORCH, CUTOUT);
//        ItemBlockRenderTypes.TYPE_BY_BLOCK.put(Blocks.WALL_TORCH, CUTOUT);
        PostProcessor.TEXTURE_UNIFORMS.put(Veil.veilPath("area"), Veil.veilPath("textures/gui/troll.png"));
    }

    private static ShaderInstance transparentTexture;
    private static final ShaderStateShard RENDERTYPE_TRANSPARENT_TEXTURE = new ShaderStateShard(() -> transparentTexture);

    public static Function<ResourceLocation, RenderType> TRANSPARENT_TEXTURE = Util.memoize(RenderTypeRegistry::createTransparentTexture);

    private static RenderType createTransparentTexture(ResourceLocation texture){
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_TRANSPARENT_TEXTURE)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(NO_OVERLAY)
                .createCompositeState(true);
        return create("transparent_texture", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, true, true, rendertype$compositestate);
    }


}
