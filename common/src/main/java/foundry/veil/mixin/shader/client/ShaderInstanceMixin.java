package foundry.veil.mixin.shader.client;

import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import foundry.veil.Veil;
import foundry.veil.api.client.render.shader.program.ShaderUniformCache;
import foundry.veil.impl.client.render.shader.processor.VanillaShaderProcessor;
import foundry.veil.impl.client.render.shader.program.ShaderProgramImpl;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL20C.*;

@Mixin(value = ShaderInstance.class, priority = 800)
public abstract class ShaderInstanceMixin implements Shader {

    @Shadow
    @Final
    private int programId;
    @Shadow
    @Final
    private List<Integer> uniformLocations;
    @Shadow
    @Final
    public Map<String, Uniform> uniformMap;
    @Shadow
    @Final
    private String name;

    @Shadow
    @Final
    private List<String> samplerNames;

    @Shadow
    @Final
    private List<Integer> samplerLocations;
    @Unique
    private final Map<String, Uniform> veil$uniforms = new Object2ObjectArrayMap<>();

    @Inject(method = "getOrCreate", at = @At("HEAD"), cancellable = true)
    private static void veil$cancelDummyProgram(ResourceProvider provider, Program.Type type, String name, CallbackInfoReturnable<Program> cir) {
        if (ShaderProgramImpl.Wrapper.constructingProgram != null) {
            cir.setReturnValue(new ShaderProgramImpl.ShaderWrapper(type, ShaderProgramImpl.Wrapper.constructingProgram));
        }
    }

    @Inject(method = "getOrCreate", at = @At("HEAD"))
    private static void veil$setupFallbackProcessor(ResourceProvider provider, Program.Type type, String name, CallbackInfoReturnable<Program> cir) {
        if (Veil.platform().hasErrors()) {
            return;
        }
        VanillaShaderProcessor.setup(provider);
    }

    @Inject(method = "getOrCreate", at = @At("RETURN"))
    private static void veil$clearFallbackProcessor(CallbackInfoReturnable<Program> cir) {
        if (Veil.platform().hasErrors()) {
            return;
        }
        VanillaShaderProcessor.free();
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void close(CallbackInfo ci) {
        if (this.veil$uniforms.isEmpty()) {
            return;
        }

        for (Uniform uniform : this.veil$uniforms.values()) {
            uniform.close();
        }
    }

    @Inject(method = "apply", at = @At("TAIL"))
    public void apply(CallbackInfo ci) {
        if (this.veil$uniforms.isEmpty()) {
            return;
        }

        for (Uniform uniform : this.veil$uniforms.values()) {
            uniform.upload();
        }
    }

    @Inject(method = "updateLocations", at = @At("HEAD"))
    public void clearSamplerLocations(CallbackInfo ci) {
        this.samplerLocations.clear();
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "updateLocations", at = @At("TAIL"))
    public void updateLocations(CallbackInfo ci) {
        if ((Object) this instanceof ShaderProgramImpl.Wrapper) {
            return;
        }

        for (Uniform uniform : this.veil$uniforms.values()) {
            uniform.setLocation(-1);
        }

        int uniformCount = glGetProgrami(this.programId, GL_ACTIVE_UNIFORMS);
        int maxUniformLength = glGetProgrami(this.programId, GL_ACTIVE_UNIFORM_MAX_LENGTH);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer size = stack.mallocInt(1);
            IntBuffer type = stack.mallocInt(1);
            for (int i = 0; i < uniformCount; i++) {
                String name = glGetActiveUniform(this.programId, i, maxUniformLength, size, type);
                int length = size.get(0);
                if (length > 1) {
                    // Cut off the trailing [0]
                    name = name.substring(0, name.length() - 3);
                }

                if (this.uniformMap.containsKey(name) || this.samplerNames.contains(name)) {
                    continue;
                }

                int dataType = type.get(0);
                String typeName = ShaderUniformCache.getName(dataType);
                if (ShaderUniformCache.isSampler(dataType)) {
                    for (int j = 0; j < length; j++) {
                        String samplerName = length > 1 ? name + '[' + j + ']' : name;
                        Veil.LOGGER.debug("Shader {} detected sampler: {}", this.name, typeName + " " + samplerName);
                        this.samplerNames.add(samplerName);
                        this.samplerLocations.add(Uniform.glGetUniformLocation(this.programId, samplerName));
                    }
                    continue;
                }

                int minecraftType;
                int minecraftCount;
                switch (dataType) {
                    case GL_INT -> {
                        minecraftType = Uniform.UT_INT1;
                        minecraftCount = 1;
                    }
                    case GL_INT_VEC2 -> {
                        minecraftType = Uniform.UT_INT2;
                        minecraftCount = 2;
                    }
                    case GL_INT_VEC3 -> {
                        minecraftType = Uniform.UT_INT3;
                        minecraftCount = 3;
                    }
                    case GL_INT_VEC4 -> {
                        minecraftType = Uniform.UT_INT4;
                        minecraftCount = 4;
                    }
                    case GL_FLOAT -> {
                        minecraftType = Uniform.UT_FLOAT1;
                        minecraftCount = 1;
                    }
                    case GL_FLOAT_VEC2 -> {
                        minecraftType = Uniform.UT_FLOAT2;
                        minecraftCount = 2;
                    }
                    case GL_FLOAT_VEC3 -> {
                        minecraftType = Uniform.UT_FLOAT3;
                        minecraftCount = 3;
                    }
                    case GL_FLOAT_VEC4 -> {
                        minecraftType = Uniform.UT_FLOAT4;
                        minecraftCount = 4;
                    }
                    case GL_FLOAT_MAT2 -> {
                        minecraftType = Uniform.UT_MAT2;
                        minecraftCount = 4;
                    }
                    case GL_FLOAT_MAT3 -> {
                        minecraftType = Uniform.UT_MAT3;
                        minecraftCount = 9;
                    }
                    case GL_FLOAT_MAT4 -> {
                        minecraftType = Uniform.UT_MAT4;
                        minecraftCount = 16;
                    }
                    default -> {
                        Veil.LOGGER.error("Unsupported Uniform Type: {}", typeName);
                        continue;
                    }
                }

                for (int j = 0; j < length; j++) {
                    String uniformName = length > 1 ? name + '[' + j + ']' : name;

                    int location = Uniform.glGetUniformLocation(this.programId, uniformName);
                    if (location == -1) {
                        // If the length is not 1, then it must be another mod adding a uniform block, so ignore
                        if (length == 1) {
                            Veil.LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", this.name, uniformName);
                        }

                        // Don't leak resources
                        Uniform old = this.veil$uniforms.remove(uniformName);
                        if (old != null) {
                            old.close();
                        }
                        continue;
                    }

                    Veil.LOGGER.debug("Shader {} detected uniform: {}", this.name, typeName + " " + uniformName);
                    Uniform old = this.veil$uniforms.get(uniformName);
                    Uniform uniform;
                    if (old != null) {
                        if (old.getType() != minecraftType) {
                            old.close();
                            this.veil$uniforms.put(uniformName, uniform = new Uniform(uniformName, minecraftType, minecraftCount, this));
                        } else {
                            uniform = old;
                        }
                    } else {
                        this.veil$uniforms.put(uniformName, uniform = new Uniform(uniformName, minecraftType, minecraftCount, this));
                    }

                    IntBuffer intBuffer = uniform.getIntBuffer();
                    if (intBuffer != null) {
                        MemoryUtil.memSet(intBuffer, 0);
                    }

                    FloatBuffer floatBuffer = uniform.getFloatBuffer();
                    if (floatBuffer != null) {
                        MemoryUtil.memSet(floatBuffer, Float.floatToIntBits(0.0F));
                    }

                    this.uniformLocations.add(location);
                    uniform.setLocation(location);
                    this.uniformMap.put(uniformName, uniform);
                }
            }
        }

        // Clean up invalid uniforms
        this.veil$uniforms.values().removeIf(uniform -> uniform.getLocation() == -1);
    }
}
