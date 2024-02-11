package foundry.veil.api.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.Veil;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public final class VeilRenderType extends RenderType {

    private static final ShaderStateShard QUASAR_PARTICLE_ADDITIVE_MULTIPLY = new ShaderStateShard(VeilVanillaShaders::getQuasarParticleAdditiveMultiply);

    private static final Function<ResourceLocation, RenderType> QUASAR_PARTICLE = Util.memoize((texture) -> {
        CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(QUASAR_PARTICLE_ADDITIVE_MULTIPLY)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);
        return create(Veil.MODID + ":quasar_particle", DefaultVertexFormat.PARTICLE, VertexFormat.Mode.QUADS, SMALL_BUFFER_SIZE, false, false, state);
    });
    private static final Function<ResourceLocation, RenderType> QUASAR_TRAIL = Util.memoize((texture) -> {
        CompositeState state = CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setCullState(NO_CULL)
                .createCompositeState(false);
        return RenderType.create(Veil.MODID + ":quasar_trail", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLE_STRIP, TRANSIENT_BUFFER_SIZE, false, true, state);
    });

    public static RenderType quasarParticle(ResourceLocation texture) {
        return QUASAR_PARTICLE.apply(texture);
    }

    public static RenderType quasarTrail(ResourceLocation texture) {
        return QUASAR_TRAIL.apply(texture);
    }

    private VeilRenderType(String $$0, VertexFormat $$1, VertexFormat.Mode $$2, int $$3, boolean $$4, boolean $$5, Runnable $$6, Runnable $$7) {
        super($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
    }
}
