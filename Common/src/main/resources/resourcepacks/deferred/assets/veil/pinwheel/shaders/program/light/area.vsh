#include veil:camera

layout (location = 0) in vec3 Position;
layout (location = 1) in mat4 LightMatrix;
layout (location = 5) in vec3 Color;
layout (location = 6) in vec2 Size;
layout (location = 7) in float Angle;
layout (location = 8) in float Distance;
layout (location = 0) in float Falloff;

out mat4 lightMat;
out vec3 lightColor;
out vec2 size;
out float maxAngle;
out float maxDistance;
out float falloff;

void main() {
    gl_Position = VeilCamera.ProjMat * VeilCamera.ViewMat * vec4(LightMatrix[3].xyz - VeilCamera.CameraPosition + Position * (max(Size.x, Size.y) + Distance), 1.0);
    lightMat = LightMatrix;
    lightColor = Color;
    size = Size;
    maxAngle = Angle;
    maxDistance = Distance;
    falloff = Falloff;
}
