package foundry.veil.api.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.shader.RenderTypeRegistry;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.function.Consumer;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.PARTICLE;

@ApiStatus.Internal
public final class VeilVanillaShaders {

    private static ShaderInstance cloud;
    private static ShaderInstance worldborder;

    public static void registerShaders(Context context) throws IOException {
        context.register(new ResourceLocation("cloud"), DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, value -> cloud = value);
        context.register(new ResourceLocation("worldborder"), DefaultVertexFormat.POSITION_TEX, value -> worldborder = value);
        context.register(new ResourceLocation("veil","quasar/particle_add"), PARTICLE, value -> RenderTypeRegistry.QUASAR_PARTICLE_ADDITIVE_MULTIPLY = value);
    }

    public static ShaderInstance getCloud() {
        return cloud;
    }

    public static ShaderInstance getWorldborder() {
        return worldborder;
    }

    @FunctionalInterface
    public interface Context {

        void register(ResourceLocation id, VertexFormat vertexFormat, Consumer<ShaderInstance> loadCallback) throws IOException;
    }
}
