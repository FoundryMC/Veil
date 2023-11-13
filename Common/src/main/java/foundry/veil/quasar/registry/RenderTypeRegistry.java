package foundry.veil.quasar.registry;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.function.Function;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.NEW_ENTITY;
import static com.mojang.blaze3d.vertex.DefaultVertexFormat.PARTICLE;

public class RenderTypeRegistry {

    public static RenderType slashFade(ResourceLocation texture) {
        return RenderTypes.SLASHFADE.apply(texture);
    }

    public static class RenderTypes extends RenderType {

        public RenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
            super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
            throw new IllegalStateException("This class should not be instantiated");
        }

        public static ShaderInstance slashFade;
        public static ShaderInstance PARTICLE_ADDITIVE_MULTIPLY;

        private static final ShaderStateShard SLASH_FADE = new ShaderStateShard(() -> slashFade);

        public static Function<ResourceLocation, RenderType> SLASHFADE = Util.memoize(RenderTypes::slashFade);

        private static RenderType slashFade(ResourceLocation texture) {
            CompositeState rendertype$compositestate = CompositeState.builder().setShaderState(SLASH_FADE).setTextureState(new TextureStateShard(texture, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).setOverlayState(OVERLAY).createCompositeState(true);
            return create("slash_fade", NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, rendertype$compositestate);
        }

        public static RenderType translucentNoCull(ResourceLocation texture) {
            return RenderType.create("quasar_translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, CompositeState.builder().setTextureState(new TextureStateShard(texture, false, false)).setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER).setWriteMaskState(RenderStateShard.COLOR_WRITE).setCullState(RenderStateShard.NO_CULL).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).createCompositeState(false));
        }
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "quasar", bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ShaderRegistry {
        @SubscribeEvent
        public static void shaderRegistry(RegisterShadersEvent event) throws IOException {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("quasar", "slash_fade"), NEW_ENTITY), shaderInstance -> {
                RenderTypes.slashFade = shaderInstance;
            });
            event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("quasar", "particle_add"), PARTICLE), shaderInstance -> {
                RenderTypes.PARTICLE_ADDITIVE_MULTIPLY = shaderInstance;
            });
        }
    }
}
