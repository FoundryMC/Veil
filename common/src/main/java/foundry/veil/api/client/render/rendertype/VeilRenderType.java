package foundry.veil.api.client.render.rendertype;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.vertex.VeilVertexFormat;
import foundry.veil.api.event.VeilRegisterFixedBuffersEvent;
import foundry.veil.impl.client.render.pipeline.CullFaceShard;
import foundry.veil.impl.client.render.rendertype.WhiteTextureStateShard;
import foundry.veil.mixin.rendertype.accessor.RenderStateShardAccessor;
import foundry.veil.mixin.rendertype.accessor.RenderTypeAccessor;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11C.*;

/**
 * Custom Veil-implemented render types.
 */
public final class VeilRenderType extends RenderType {

    public static final RenderStateShard.DepthTestStateShard NEVER_DEPTH_TEST = new RenderStateShard.DepthTestStateShard("never", GL_NEVER);
    public static final RenderStateShard.DepthTestStateShard LESS_DEPTH_TEST = new RenderStateShard.DepthTestStateShard("<", GL_LESS);
    public static final RenderStateShard.DepthTestStateShard NOTEQUAL_DEPTH_TEST = new RenderStateShard.DepthTestStateShard("<", GL_NOTEQUAL);
    public static final RenderStateShard.DepthTestStateShard GEQUAL_DEPTH_TEST = new RenderStateShard.DepthTestStateShard(">=", GL_GEQUAL);

    public static final RenderStateShard CULL_FRONT = new CullFaceShard(GL_FRONT);
    public static final RenderStateShard CULL_BACK = new CullFaceShard(GL_BACK);
    public static final RenderStateShard CULL_FRONT_AND_BACK = new CullFaceShard(GL_FRONT_AND_BACK);
    public static final RenderStateShard.WriteMaskStateShard NO_WRITE = new RenderStateShard.WriteMaskStateShard(false, false);
    public static final RenderStateShard.EmptyTextureStateShard WHITE_TEXTURE = new WhiteTextureStateShard();

    private static final EnumMap<GlStateManager.LogicOp, ColorLogicStateShard> COLOR_LOGIC_SHARDS = new EnumMap<>(GlStateManager.LogicOp.class);

    static {
        for (GlStateManager.LogicOp logicOp : GlStateManager.LogicOp.values()) {
            COLOR_LOGIC_SHARDS.put(logicOp, new ColorLogicStateShard(logicOp.name().toLowerCase(Locale.ROOT), () -> {
                RenderSystem.enableColorLogicOp();
                RenderSystem.logicOp(logicOp);
            }, RenderSystem::disableColorLogicOp));
        }
    }

    private static final ShaderStateShard PARTICLE = VeilRenderBridge.shaderState(Veil.veilPath("quasar/particle"));
    private static final ShaderStateShard PARTICLE_ADDITIVE = VeilRenderBridge.shaderState(Veil.veilPath("quasar/particle_additive"));

    private static final BiFunction<ResourceLocation, Boolean, RenderType> QUASAR_PARTICLE = Util.memoize((texture, additive) -> {
        CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(additive ? PARTICLE_ADDITIVE : PARTICLE)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(additive ? ADDITIVE_TRANSPARENCY : TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .createCompositeState(false);
        return create(Veil.MODID + ":quasar_particle", VeilVertexFormat.QUASAR_PARTICLE, VertexFormat.Mode.QUADS, SMALL_BUFFER_SIZE, false, false, state);
    });
    private static final BiFunction<ResourceLocation, Boolean, RenderType> QUASAR_TRAIL = Util.memoize((texture, additive) -> {
        CompositeState state = CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(additive ? ADDITIVE_TRANSPARENCY : TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .setCullState(NO_CULL)
                .createCompositeState(false);
        return RenderType.create(Veil.MODID + ":quasar_trail", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLE_STRIP, TRANSIENT_BUFFER_SIZE, false, false, state);
    });
    private static final Function<Boolean, RenderType> NO_TEXTURE_QUASAR_TRAIL = Util.memoize((additive) -> {
        CompositeState state = CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setTextureState(WHITE_TEXTURE)
                .setTransparencyState(additive ? ADDITIVE_TRANSPARENCY : TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .setCullState(NO_CULL)
                .createCompositeState(false);
        return RenderType.create(Veil.MODID + ":quasar_trail", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLE_STRIP, TRANSIENT_BUFFER_SIZE, false, false, state);
    }) ;

    public static RenderType quasarParticle(ResourceLocation texture, boolean additive) {
        return QUASAR_PARTICLE.apply(texture, additive);
    }

    public static RenderType quasarTrail(@Nullable ResourceLocation texture) {
        return quasarTrail(texture, true);
    }

    public static RenderType quasarTrail(@Nullable ResourceLocation texture, boolean additive) {
        return texture != null ? QUASAR_TRAIL.apply(texture, additive) : NO_TEXTURE_QUASAR_TRAIL.apply(additive);
    }

    public static TransparencyStateShard noTransparencyShard() {
        return RenderStateShard.NO_TRANSPARENCY;
    }

    public static TransparencyStateShard additiveTransparencyShard() {
        return RenderStateShard.ADDITIVE_TRANSPARENCY;
    }

    public static TransparencyStateShard lightningTransparencyShard() {
        return RenderStateShard.LIGHTNING_TRANSPARENCY;
    }

    public static TransparencyStateShard glintTransparencyShard() {
        return RenderStateShard.GLINT_TRANSPARENCY;
    }

    public static TransparencyStateShard crumblingTransparencyShard() {
        return RenderStateShard.CRUMBLING_TRANSPARENCY;
    }

    public static TransparencyStateShard translucentTransparencyShard() {
        return RenderStateShard.TRANSLUCENT_TRANSPARENCY;
    }

    public static DepthTestStateShard noDepthTestShard() {
        return RenderStateShard.NO_DEPTH_TEST;
    }

    public static DepthTestStateShard equalDepthTestShard() {
        return RenderStateShard.EQUAL_DEPTH_TEST;
    }

    public static DepthTestStateShard lequalDepthTestShard() {
        return RenderStateShard.LEQUAL_DEPTH_TEST;
    }

    public static DepthTestStateShard greaterDepthTestShard() {
        return RenderStateShard.GREATER_DEPTH_TEST;
    }

    public static CullStateShard cullShard() {
        return RenderStateShard.CULL;
    }

    public static CullStateShard noCullShard() {
        return RenderStateShard.NO_CULL;
    }

    public static LightmapStateShard lightmap() {
        return RenderStateShard.LIGHTMAP;
    }

    public static LightmapStateShard noLightmap() {
        return RenderStateShard.NO_LIGHTMAP;
    }

    public static OverlayStateShard overlay() {
        return RenderStateShard.OVERLAY;
    }

    public static OverlayStateShard noOverlay() {
        return RenderStateShard.NO_OVERLAY;
    }

    public static LayeringStateShard noLayering() {
        return RenderStateShard.NO_LAYERING;
    }

    public static LayeringStateShard polygonOffsetLayering() {
        return RenderStateShard.POLYGON_OFFSET_LAYERING;
    }

    public static LayeringStateShard viewOffsetLayering() {
        return RenderStateShard.VIEW_OFFSET_Z_LAYERING;
    }

    public static WriteMaskStateShard colorDepthWriteShard() {
        return RenderStateShard.COLOR_DEPTH_WRITE;
    }

    public static WriteMaskStateShard colorWriteShard() {
        return RenderStateShard.COLOR_WRITE;
    }

    public static WriteMaskStateShard depthWriteShard() {
        return RenderStateShard.DEPTH_WRITE;
    }

    public static ColorLogicStateShard colorLogicStateShard(GlStateManager.LogicOp op) {
        return COLOR_LOGIC_SHARDS.get(op);
    }

    /**
     * Retrieves and caches a render type with the specified id.
     *
     * @param id     The id of the render type to get
     * @param params Additional parameters to configure the render type
     * @return The render type created or <code>null</code> if unregistered or an error occurs
     */
    @Contract(pure = true)
    public static @Nullable RenderType get(ResourceLocation id, Object... params) {
        return VeilRenderSystem.renderer().getDynamicRenderTypeManager().get(id, params);
    }

    /**
     * Creates a new wrapper pointing to a.
     *
     * @param id The id of the render type to wrap
     * @return A wrapper render type that points to the specified dynamic render type
     * @since 2.0.0
     */
    @Contract(value = "_->new", pure = true)
    public static RenderTypeWrapper getWrapper(ResourceLocation id) {
        return new RenderTypeWrapper(id);
    }

    /**
     * Retrieves the name of the specified render shard.
     *
     * @param shard The render shard to get the name of
     * @return The name of the render type to get
     */
    @Contract(pure = true)
    public static String getName(RenderStateShard shard) {
        return ((RenderStateShardAccessor) shard).getName();
    }

    @Contract(pure = true)
    public static VeilRenderTypeAccessor getShards(RenderType renderType) {
        if (!(renderType instanceof CompositeRenderType compositeRenderType)) {
            throw new IllegalArgumentException("Expected composite render type to be an instance of " + CompositeRenderType.class.getName() + ", but was " + renderType.getClass());
        }
        return (VeilRenderTypeAccessor) (Object) compositeRenderType.state();
    }

    /**
     * Creates a render type that uses a single draw buffer, but re-uses the data to draw the specified layers.
     *
     * @param layers The layers to use
     * @return A render type that draws all layers from a single buffer
     * @throws IllegalStateException If there are zero layers, the vertex formats don't all match, or the primitive modes don't match
     */
    @Contract(pure = true)
    public static RenderType layered(RenderType... layers) {
        if (layers.length == 0) {
            throw new IllegalArgumentException("At least 1 render type must be specified");
        }
        if (layers.length == 1) {
            return layers[0];
        }

        RenderType[] array = new RenderType[layers.length - 1];
        VertexFormat format = layers[0].format();
        VertexFormat.Mode mode = layers[0].mode();
        int bufferSize = layers[0].bufferSize();
        boolean sortOnUpload = ((RenderTypeAccessor) layers[0]).isSortOnUpload();

        for (int i = 1; i < layers.length; i++) {
            RenderType layer = layers[i];
            if (!layer.format().equals(format)) {
                throw new IllegalArgumentException("Expected " + layer + " to use " + format + ", but was " + layer.format());
            }
            if (!layer.mode().equals(mode)) {
                throw new IllegalArgumentException("Expected " + layer + " to use " + mode + ", but was " + layer.mode());
            }
            bufferSize = Math.max(bufferSize, layer.bufferSize());
            if (((RenderTypeAccessor) layer).isSortOnUpload()) {
                sortOnUpload = true;
            }
            array[i - 1] = layer;
        }
        return new LayeredRenderType(layers[0], array, "LayeredRenderType[" + Arrays.stream(layers).map(VeilRenderType::getName).collect(Collectors.joining(", ")) + "]", bufferSize, sortOnUpload);
    }

    /**
     * Layers multiple render types on top of each other to re-use the same mesh data when rendering.
     * <br>
     * Essentially, this acts as "render passes" for a render type.
     */
    public static class LayeredRenderType extends RenderType {

        private final RenderType[] layers;

        private LayeredRenderType(RenderType defaultValue, RenderType[] layers, String name, int bufferSize, boolean sortOnUpload) {
            super(name, defaultValue.format(), defaultValue.mode(), bufferSize, defaultValue.affectsCrumbling(), sortOnUpload, defaultValue::setupRenderState, defaultValue::clearRenderState);
            this.layers = layers;
        }

        @Override
        public void draw(@NotNull MeshData meshData) {
            super.draw(meshData);
            if (BufferUploader.lastImmediateBuffer != null) {

                Matrix4f modelViewMatrix = RenderSystem.getModelViewMatrix();
                Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
                for (RenderType layer : this.layers) {
                    layer.setupRenderState();

                    ShaderInstance shader = RenderSystem.getShader();
                    if (shader != null) {
                        BufferUploader.lastImmediateBuffer.drawWithShader(modelViewMatrix, projectionMatrix, shader);
                    }

                    layer.clearRenderState();
                }
            }
        }

        /**
         * @return All additional render layers this render type should render
         */
        public RenderType[] getLayers() {
            return this.layers;
        }
    }

    /**
     * Wraps a Veil dynamic render type with a static render type. Useful for {@link net.neoforged.neoforge.client.event.RegisterNamedRenderTypesEvent} or {@link VeilRegisterFixedBuffersEvent}.
     *
     * @since 2.0.0
     */
    public static class RenderTypeWrapper extends RenderType {

        private static final Object[] NO_PARAMS = new Object[0];

        private final ResourceLocation id;
        private Object[] params;

        private RenderTypeWrapper(ResourceLocation id) {
            super(id.toString(), DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 0, false, false, () -> {
            }, () -> {
            });
            this.id = id;
            this.params = NO_PARAMS;
        }

        @Override
        public void setupRenderState() {
            RenderType renderType = this.get();
            if (renderType != null) {
                renderType.setupRenderState();
            }
        }

        @Override
        public void clearRenderState() {
            RenderType renderType = this.get();
            if (renderType != null) {
                renderType.clearRenderState();
            }
        }

        @Override
        public void draw(@NotNull MeshData meshData) {
            RenderType renderType = this.get();
            if (renderType != null) {
                renderType.draw(meshData);
            }
        }

        @Override
        public int bufferSize() {
            RenderType renderType = this.get();
            return renderType != null ? renderType.bufferSize() : TRANSIENT_BUFFER_SIZE;
        }

        @Override
        public @NotNull VertexFormat format() {
            RenderType renderType = this.get();
            return renderType != null ? renderType.format() : DefaultVertexFormat.POSITION;
        }

        @Override
        public VertexFormat.@NotNull Mode mode() {
            RenderType renderType = this.get();
            return renderType != null ? renderType.mode() : VertexFormat.Mode.QUADS;
        }

        @Override
        public @NotNull Optional<RenderType> outline() {
            RenderType renderType = this.get();
            return renderType != null ? renderType.outline() : Optional.empty();
        }

        @Override
        public boolean isOutline() {
            RenderType renderType = this.get();
            return renderType != null && renderType.isOutline();
        }

        @Override
        public boolean affectsCrumbling() {
            RenderType renderType = this.get();
            return renderType != null && renderType.affectsCrumbling();
        }

        @Override
        public boolean canConsolidateConsecutiveGeometry() {
            RenderType renderType = this.get();
            return renderType != null && renderType.canConsolidateConsecutiveGeometry();
        }

        @Override
        public boolean sortOnUpload() {
            RenderType renderType = this.get();
            return renderType != null && renderType.sortOnUpload();
        }

        /**
         * Sets the parameters to pass to the render type.
         *
         * @param params The new parameters
         */
        public void setParams(Object... params) {
            if (params.length == 0) {
                this.params = NO_PARAMS;
            } else if (this.params.length == params.length) {
                System.arraycopy(params, 0, this.params, 0, params.length);
            } else {
                this.params = Arrays.copyOf(params, params.length);
            }
        }

        /**
         * @return The dynamic render type instance or <code>null</code> if it failed to load
         */
        public @Nullable RenderType get() {
            return VeilRenderSystem.renderer().getDynamicRenderTypeManager().get(this.id, this.params);
        }
    }

    private VeilRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
}
