package foundry.veil.postprocessing;

import com.mojang.blaze3d.shaders.Uniform;

public interface ExpandedShaderInstance {
    Uniform getCameraPositionUniform();
    Uniform getLookVectorUniform();
    Uniform getUpVectorUniform();
    Uniform getLeftVectorUniform();
    Uniform getInverseViewMatrixUniform();
    Uniform getInverseProjectionMatrixUniform();
    Uniform getNearPlaneDistanceUniform();
    Uniform getFarPlaneDistanceUniform();
    Uniform getFovUniform();
    Uniform getAspectRatioUniform();
    Uniform getPerspectiveUniform();
    void setCAMERAPOSITION(Uniform cameraPosition);
    void setLOOK_VECTOR(Uniform lookVector);
    void setUP_VECTOR(Uniform upVector);
    void setLEFT_VECTOR(Uniform leftVector);
    void setINVERSE_VIEW_MATRIX(Uniform inverseViewMatrix);
    void setINVERSE_PROJECTION_MATRIX(Uniform inverseProjectionMatrix);
    void setNEAR_PLANE_DISTANCE(Uniform nearPlaneDistance);
    void setFAR_PLANE_DISTANCE(Uniform farPlaneDistance);
    void setFOV(Uniform fov);
    void setASPECT_RATIO(Uniform aspectRatio);
    void setPERSPECTIVE(Uniform perspective);

}
