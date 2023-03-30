package foundry.veil.postprocessing;

import foundry.veil.mixin.client.ShaderInstanceMixin;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ShaderHelper {
    public static void initUniforms(ShaderInstance shader) {
        ExpandedShaderInstance expandedShaderInstance = (ExpandedShaderInstance) shader;
        expandedShaderInstance.setCAMERAPOSITION(shader.getUniform("CameraPosition"));
        expandedShaderInstance.setLOOK_VECTOR(shader.getUniform("LookVector"));
        expandedShaderInstance.setUP_VECTOR(shader.getUniform("UpVector"));
        expandedShaderInstance.setLEFT_VECTOR(shader.getUniform("LeftVector"));
        expandedShaderInstance.setINVERSE_VIEW_MATRIX(shader.getUniform("InverseViewMatrix"));
        expandedShaderInstance.setINVERSE_PROJECTION_MATRIX(shader.getUniform("InverseProjectionMatrix"));
        expandedShaderInstance.setNEAR_PLANE_DISTANCE(shader.getUniform("NearPlaneDistance"));
        expandedShaderInstance.setFAR_PLANE_DISTANCE(shader.getUniform("FarPlaneDistance"));
        expandedShaderInstance.setFOV(shader.getUniform("Fov"));
        expandedShaderInstance.setASPECT_RATIO(shader.getUniform("AspectRatio"));
        expandedShaderInstance.setPERSPECTIVE(shader.getUniform("Perspective"));

        PostProcessor.COMMON_UNIFORMS.forEach(uniform -> {
            if (uniform.getFirst().equals("CameraPosition")) {
                if(expandedShaderInstance.getCameraPositionUniform() != null)
                    uniform.getSecond().accept(expandedShaderInstance.getCameraPositionUniform());
            } else if (uniform.getFirst().equals("LookVector")) {
                if (expandedShaderInstance.getLookVectorUniform() != null)
                    uniform.getSecond().accept(expandedShaderInstance.getLookVectorUniform());
            } else if (uniform.getFirst().equals("UpVector")) {
                if (expandedShaderInstance.getUpVectorUniform() != null)
                    uniform.getSecond().accept(expandedShaderInstance.getUpVectorUniform());
            } else if (uniform.getFirst().equals("LeftVector")) {
                if (expandedShaderInstance.getLeftVectorUniform() != null)
                    uniform.getSecond().accept(expandedShaderInstance.getLeftVectorUniform());
            } else if (uniform.getFirst().equals("InverseViewMatrix")) {
                if (expandedShaderInstance.getInverseViewMatrixUniform() != null)
                    uniform.getSecond().accept(expandedShaderInstance.getInverseViewMatrixUniform());
            } else if (uniform.getFirst().equals("InverseProjectionMatrix")) {
                if (expandedShaderInstance.getInverseProjectionMatrixUniform() != null)
                    uniform.getSecond().accept(expandedShaderInstance.getInverseProjectionMatrixUniform());
            } else if (uniform.getFirst().equals("NearPlaneDistance")) {
                if (expandedShaderInstance.getNearPlaneDistanceUniform() != null)
                    uniform.getSecond().accept(expandedShaderInstance.getNearPlaneDistanceUniform());
            } else if (uniform.getFirst().equals("FarPlaneDistance")) {
                if (expandedShaderInstance.getFarPlaneDistanceUniform() != null)
                    uniform.getSecond().accept(expandedShaderInstance.getFarPlaneDistanceUniform());
            } else if (uniform.getFirst().equals("Fov")) {
                if (expandedShaderInstance.getFovUniform() != null)
                    uniform.getSecond().accept(expandedShaderInstance.getFovUniform());
            } else if (uniform.getFirst().equals("AspectRatio")) {
                if (expandedShaderInstance.getAspectRatioUniform() != null)
                    uniform.getSecond().accept(expandedShaderInstance.getAspectRatioUniform());
            } else if (uniform.getFirst().equals("Perspective")) {
                if (expandedShaderInstance.getPerspectiveUniform() != null)
                    uniform.getSecond().accept(expandedShaderInstance.getPerspectiveUniform());
            }
        });

    }
}