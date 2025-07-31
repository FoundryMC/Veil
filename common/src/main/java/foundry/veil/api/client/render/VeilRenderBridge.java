package foundry.veil.api.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.rendertype.VeilRenderTypeBuilder;
import foundry.veil.api.client.render.shader.ShaderManager;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.impl.client.render.pipeline.AdvancedFboShard;
import foundry.veil.impl.client.render.pipeline.FlagShards;
import foundry.veil.impl.client.render.pipeline.PatchStateShard;
import foundry.veil.impl.client.render.pipeline.ShaderProgramShard;
import foundry.veil.impl.client.render.shader.program.ShaderProgramImpl;
import foundry.veil.impl.client.render.wrapper.DSAVanillaAdvancedFboWrapper;
import foundry.veil.impl.client.render.wrapper.LegacyVanillaAdvancedFboWrapper;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Bridges between Minecraft and Veil render classes.
 *
 * @author Ocelot
 */
public interface VeilRenderBridge {

    /**
     * <p>Wraps the specified shader with a vanilla Minecraft shader instance wrapper. There are a few special properties about the shader wrapper.</p>
     * <ul>
     *     <li>The shader instance cannot be used to free the shader program. {@link ShaderProgram#free()} must be called separately.
     *     If the shader is loaded through {@link ShaderManager} then there is no need to free the shader.</li>
     *     <li>Calling {@link Uniform#upload()} will do nothing since the values are uploaded when the appropriate methods are called</li>
     *     <li>Uniforms are lazily wrapped and will not crash when the wrong method is called.</li>
     *     <li>{@link Uniform#set(int, float)} is not supported and will throw an {@link UnsupportedOperationException}.</li>
     *     <li>{@link Uniform#set(float[])} only works for 1, 2, 3, and 4 float elements. Any other size will throw an {@link UnsupportedOperationException}.</li>
     * </ul>
     *
     * @param program The program to create a shader instance from
     * @return A lazily loaded shader instance wrapper for this program
     */
    static ShaderInstance toShaderInstance(ShaderProgram program) {
        return ((ShaderProgramImpl) program).toShaderInstance();
    }

    /**
     * Creates a cull frustum helper from the specified vanilla frustum.
     *
     * @param frustum The frustum to use for the cull frustum
     * @return The cull frustum
     */
    static CullFrustum create(Frustum frustum) {
        return (CullFrustum) frustum;
    }

    /**
     * Creates a render type builder helper from the specified vanilla composite state builder.
     *
     * @param builder The state builder to wrap
     * @return The render type builder
     */
    static VeilRenderTypeBuilder create(RenderType.CompositeState.CompositeStateBuilder builder) {
        return (VeilRenderTypeBuilder) builder;
    }

    /**
     * Creates a matrix stack wrapper for the specified post stack.
     *
     * @param poseStack The pose stack to wrap
     * @return The matrix stack representation
     */
    static MatrixStack create(PoseStack poseStack) {
        return (MatrixStack) poseStack;
    }

    /**
     * Wraps the specified render target in a new advanced fbo.
     *
     * @param renderTarget The render target instance
     * @return A new advanced fbo that wraps the target in the api
     */
    static AdvancedFbo wrap(RenderTarget renderTarget) {
        return VeilRenderBridge.wrap(() -> renderTarget);
    }

    /**
     * Wraps the specified render target in a new advanced fbo.
     *
     * @param renderTargetSupplier The supplier to the render target instance
     * @return A new advanced fbo that wraps the target in the api
     */
    static AdvancedFbo wrap(Supplier<RenderTarget> renderTargetSupplier) {
        return VeilRenderSystem.directStateAccessSupported() ? new DSAVanillaAdvancedFboWrapper(renderTargetSupplier) : new LegacyVanillaAdvancedFboWrapper(renderTargetSupplier);
    }

    /**
     * Creates a new shader state that points to the specified Veil shader name.
     *
     * @param shader The name of the shader to point to.
     * @return A new shader state shard for that shader
     */
    static RenderStateShard.ShaderStateShard shaderState(ResourceLocation shader) {
        return new ShaderProgramShard(shader);
    }

    /**
     * Creates a new output state that draws into the specified Veil framebuffer.
     *
     * @param framebuffer The framebuffer to use
     * @return A new shader state shard for that shader
     */
    static RenderStateShard.OutputStateShard outputState(ResourceLocation framebuffer) {
        return new AdvancedFboShard(framebuffer, () -> VeilRenderSystem.renderer().getFramebufferManager().getFramebuffer(framebuffer));
    }

    /**
     * Creates a new output state that draws into the specified Veil framebuffer.
     *
     * @param framebuffer The framebuffer to use
     * @return A new shader state shard for that shader
     */
    static RenderStateShard.OutputStateShard outputState(AdvancedFbo framebuffer) {
        return new AdvancedFboShard(null, () -> framebuffer);
    }

    /**
     * Creates a new output state that draws into the specified Veil framebuffer.
     *
     * @param framebuffer A supplier to the framebuffer to use
     * @return A new shader state shard for that shader
     */
    static RenderStateShard.OutputStateShard outputState(Supplier<AdvancedFbo> framebuffer) {
        return new AdvancedFboShard(null, framebuffer);
    }

    /**
     * Creates a new render state shard for tesselation patch size.
     *
     * @param patchVertices The number of vertices per patch
     * @return A new patch state
     */
    static RenderStateShard patchState(int patchVertices) {
        return new PatchStateShard(patchVertices);
    }

    /**
     * @return A render state shard to enable depth clamp
     * @since 2.0.0
     */
    static RenderStateShard depthClampState() {
        return FlagShards.DEPTH_CLAMP;
    }

    /**
     * @return A render state shard to enable color dithering
     * @since 2.0.0
     */
    static RenderStateShard ditherState() {
        return FlagShards.DITHER;
    }

    /**
     * @return A render state shard to enable smooth line rendering
     * @since 2.0.0
     */
    static RenderStateShard lineSmoothState() {
        return FlagShards.LINE_SMOOTH;
    }

    /**
     * @return A render state shard to enable multisampling
     * @since 2.0.0
     */
    static RenderStateShard multisampleState() {
        return FlagShards.MULTISAMPLE;
    }

    /**
     * @return A render state shard to enable seamless cube map textures
     * @since 2.0.0
     */
    static RenderStateShard seamlessCubeMapState() {
        return FlagShards.TEXTURE_CUBE_MAP_SEAMLESS;
    }
}
