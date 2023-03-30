package foundry.veil.mixin.client;

import com.mojang.blaze3d.shaders.Uniform;
import foundry.veil.postprocessing.ExpandedShaderInstance;
import foundry.veil.postprocessing.ShaderHelper;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShaderInstance.class)
public class ShaderInstanceMixin implements ExpandedShaderInstance {
    @Unique
    public Uniform CAMERA_POSITION;
    @Unique
    public Uniform LOOK_VECTOR;
    @Unique
    public Uniform UP_VECTOR;
    @Unique
    public Uniform LEFT_VECTOR;
    @Unique
    public Uniform INVERSE_VIEW_MATRIX;
    @Unique
    public Uniform INVERSE_PROJECTION_MATRIX;
    @Unique
    public Uniform NEAR_PLANE_DISTANCE;
    @Unique
    public Uniform FAR_PLANE_DISTANCE;
    @Unique
    public Uniform FOV;
    @Unique
    public Uniform ASPECT_RATIO;
    @Unique
    public Uniform PERSPECTIVE;

    @Override
    public Uniform getCameraPositionUniform() {
        return CAMERA_POSITION;
    }

    @Override
    public Uniform getLookVectorUniform() {
        return LOOK_VECTOR;
    }

    @Override
    public Uniform getUpVectorUniform() {
        return UP_VECTOR;
    }

    @Override
    public Uniform getLeftVectorUniform() {
        return LEFT_VECTOR;
    }

    @Override
    public Uniform getInverseViewMatrixUniform() {
        return INVERSE_VIEW_MATRIX;
    }

    @Override
    public Uniform getInverseProjectionMatrixUniform() {
        return INVERSE_PROJECTION_MATRIX;
    }

    @Override
    public Uniform getNearPlaneDistanceUniform() {
        return NEAR_PLANE_DISTANCE;
    }

    @Override
    public Uniform getFarPlaneDistanceUniform() {
        return FAR_PLANE_DISTANCE;
    }

    @Override
    public Uniform getFovUniform() {
        return FOV;
    }

    @Override
    public Uniform getAspectRatioUniform() {
        return ASPECT_RATIO;
    }

    @Override
    public Uniform getPerspectiveUniform() {
        return PERSPECTIVE;
    }

    public void setCAMERAPOSITION(Uniform cameraPosition) {
        this.CAMERA_POSITION = cameraPosition;
    }

    public void setLOOK_VECTOR(Uniform lookVector) {
        this.LOOK_VECTOR = lookVector;
    }

    public void setUP_VECTOR(Uniform upVector) {
        this.UP_VECTOR = upVector;
    }

    public void setLEFT_VECTOR(Uniform leftVector) {
        this.LEFT_VECTOR = leftVector;
    }

    public void setINVERSE_VIEW_MATRIX(Uniform inverseViewMatrix) {
        this.INVERSE_VIEW_MATRIX = inverseViewMatrix;
    }

    public void setINVERSE_PROJECTION_MATRIX(Uniform inverseProjectionMatrix) {
        this.INVERSE_PROJECTION_MATRIX = inverseProjectionMatrix;
    }

    public void setNEAR_PLANE_DISTANCE(Uniform nearPlaneDistance) {
        this.NEAR_PLANE_DISTANCE = nearPlaneDistance;
    }

    public void setFAR_PLANE_DISTANCE(Uniform farPlaneDistance) {
        this.FAR_PLANE_DISTANCE = farPlaneDistance;
    }

    public void setFOV(Uniform fov) {
        this.FOV = fov;
    }

    public void setASPECT_RATIO(Uniform aspectRatio) {
        this.ASPECT_RATIO = aspectRatio;
    }

    public void setPERSPECTIVE(Uniform perspective) {
        this.PERSPECTIVE = perspective;
        System.out.println("PERSPECTIVE: " + perspective);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        ShaderInstance shaderInstance = (ShaderInstance) (Object) this;
        ShaderHelper.initUniforms(shaderInstance);
    }
}
