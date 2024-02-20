package foundry.veil.api.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.Veil;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.function.Consumer;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.PARTICLE;

@ApiStatus.Internal
public final class VeilVanillaShaders {

    private static ShaderInstance clouds;
    private static ShaderInstance worldborder;
    private static ShaderInstance quasarParticleAdditiveMultiply;

    public static void registerShaders(Context context) throws IOException {
        if (!Veil.platform().isSodiumLoaded()) {
            context.register(new ResourceLocation("clouds"), DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, value -> clouds = value);
        }
        context.register(new ResourceLocation("worldborder"), DefaultVertexFormat.POSITION_TEX, value -> worldborder = value);
        // TODO replace with veil shader
        context.register(new ResourceLocation("veil", "quasar/particle_add"), PARTICLE, value -> quasarParticleAdditiveMultiply = value);
    }

    public static ShaderInstance getClouds() {
        return clouds;
    }

    public static ShaderInstance getWorldborder() {
        return worldborder;
    }

    public static ShaderInstance getQuasarParticleAdditiveMultiply() {
        return quasarParticleAdditiveMultiply;
    }

    @FunctionalInterface
    public interface Context {

        void register(ResourceLocation id, VertexFormat vertexFormat, Consumer<ShaderInstance> loadCallback) throws IOException;
    }
}
