package foundry.veil.shader;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Blocks;

import static net.minecraft.client.renderer.RenderStateShard.*;
import static net.minecraft.client.renderer.RenderType.create;

public class RenderTypeRegistry {
    private static final RenderType CUTOUT = RenderType.create("cutout", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 131072, true, false, RenderType.CompositeState.builder().setOutputState(RenderStateShardRegistry.VEIL_CUSTOM).setLightmapState(LIGHTMAP).setShaderState(RENDERTYPE_CUTOUT_SHADER).setTextureState(BLOCK_SHEET).createCompositeState(true));
    public static void init(){
        RenderTargetRegistry.register("veil_custom", new TextureTarget(Minecraft.getInstance().getMainRenderTarget().width, Minecraft.getInstance().getMainRenderTarget().height, true, Minecraft.ON_OSX), true);
        ItemBlockRenderTypes.TYPE_BY_BLOCK.remove(Blocks.TORCH);
        ItemBlockRenderTypes.TYPE_BY_BLOCK.remove(Blocks.WALL_TORCH);
        ItemBlockRenderTypes.TYPE_BY_BLOCK.put(Blocks.TORCH, CUTOUT);
        ItemBlockRenderTypes.TYPE_BY_BLOCK.put(Blocks.WALL_TORCH, CUTOUT);
    }
}
