package foundry.veil.mixin.pipeline.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Arrays;

@Mixin(ShaderInstance.class)
public abstract class PipelineShaderInstanceMixin {

    @Unique
    private static final Direction[] veil$DIRECTIONS = Direction.values();
    @Unique
    private static final String[] veil$FACE_BRIGHTNESS_UNIFORM_NAMES = Arrays.stream(veil$DIRECTIONS)
            .map(direction -> "VeilBlockFaceBrightness[" + direction.get3DDataValue() + "]")
            .toArray(String[]::new);

    @Shadow
    @Nullable
    public abstract Uniform getUniform(String name);

    @Inject(method = "setDefaultUniforms", at = @At("TAIL"))
    public void setDefaultUniforms(VertexFormat.Mode mode, Matrix4f projectionMatrix, Matrix4f frustrumMatrix, Window window, CallbackInfo ci) {
        Uniform renderTime = this.getUniform("VeilRenderTime");
        if (renderTime != null) {
            renderTime.set((System.currentTimeMillis() % 3_600_000) / 1000.0F);
        }

        Uniform iModelViewMat = this.getUniform("NormalMat");
        if (iModelViewMat != null) {
            iModelViewMat.set(projectionMatrix.normal(new Matrix3f()));
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            for (Direction value : veil$DIRECTIONS) {
                Uniform uniform = this.getUniform(veil$FACE_BRIGHTNESS_UNIFORM_NAMES[value.ordinal()]);
                if (uniform != null) {
                    uniform.set(level.getShade(value, true));
                }
            }
        }
    }
}
