package foundry.veil.api.client.render.shader;

import com.mojang.serialization.Codec;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.util.EnumCodec;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Shader features allow a higher-level way of requesting GLSL functionality without having to manually enable extensions per-shader.
 *
 * @since 2.0.0
 */
public enum ShaderFeature {
    COMPUTE,
    SHADER_STORAGE,
    ATOMIC_COUNTER,
    BINDLESS_TEXTURE,
    /**
     * @since 2.1.0
     */
    CUBE_MAP_ARRAY;

    @ApiStatus.Internal
    public static final ShaderFeature[] FEATURES = values();
    public static final Codec<ShaderFeature> CODEC = EnumCodec.<ShaderFeature>builder("Shader Feature")
            .values(values())
            .build();

    private final String definitionName;

    ShaderFeature() {
        this.definitionName = "SHADER_FEATURE_" + this.name();
    }

    /**
     * @return Whether this feature is supported on this platform
     */
    public boolean isSupported() {
        return switch (this) {
            case COMPUTE -> VeilRenderSystem.computeSupported();
            case SHADER_STORAGE -> VeilRenderSystem.shaderStorageBufferSupported();
            case ATOMIC_COUNTER -> VeilRenderSystem.atomicCounterSupported();
            case BINDLESS_TEXTURE -> VeilRenderSystem.bindlessTextureSupported();
            case CUBE_MAP_ARRAY -> VeilRenderSystem.textureCubeMapArraySupported();
        };
    }

    /**
     * @return The shader definition name in GLSL code
     */
    public String getDefinitionName() {
        return this.definitionName;
    }

    /**
     * Modifies the specified shader source to add the required GLSL extensions.
     *
     * @param tree The tree to modify
     */
    public void modifyShader(GlslTree tree) {
        final List<String> directives = tree.getDirectives();
        switch (this) {
            case COMPUTE -> directives.add("#extension GL_ARB_compute_shader : require");
            case SHADER_STORAGE -> directives.add("#extension GL_ARB_shader_storage_buffer_object : require");
            case ATOMIC_COUNTER -> directives.add("#extension GL_ARB_shader_atomic_counters : require");
            case BINDLESS_TEXTURE -> {
                directives.add("#extension GL_NV_gpu_shader5 : enable");
                directives.add("#extension GL_EXT_nonuniform_qualifier : enable");
            }
        }
    }
}
