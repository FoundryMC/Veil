#veil:buffer veil:camera VeilCamera

layout (location = 0) in vec3 Position;
layout (location = 1) in mat4 LightMatrix;
layout (location = 5) in vec3 Color;
#ifdef SPOTLIGHT
layout (location = 6) in float Size;
#else
layout (location = 6) in vec2 Size;
#endif
layout (location = 7) in float NormalizedAngle;
layout (location = 8) in vec3 Settings;

#define Distance Settings.x
#define Occluded Settings.y
#define Inscattering Settings.z

out mat4 lightMat;
out mat4 invLightMat;
out vec3 lightColor;
#ifdef SPOTLIGHT
out float size;
#else
out vec2 size;
#endif
out float maxAngle;
out float maxDistance;
out float occluded;
#ifdef INSCATTERING
out float inscattering;
#endif

void main() {
    vec3 vertexPos = Position;
    float Angle = NormalizedAngle * 6.28318530718;

    #ifdef SPOTLIGHT
    float term = Size;
    #else
    float term = Angle;
    #endif
    vertexPos.z = clamp(vertexPos.z, min(cos(term), 0), 1);
    float angleTerm = sin(min(term, 1.57079633)) * Distance;

    #ifdef SPOTLIGHT
    vertexPos *= vec3(angleTerm, angleTerm, Distance);
    #else
    vertexPos *= vec3(Size.x + angleTerm, Size.y + angleTerm, Distance);
    #endif

    // awful fix but not sure why just multiplying the matrix doesnt work? it does what it should in
    // all the second calculations. really weird!
    vec3 lightPos = LightMatrix[3].xyz;
    mat3 rotationMatrix = mat3(LightMatrix);
    lightPos = inverse(rotationMatrix) * lightPos;
    vertexPos = inverse(rotationMatrix) * vertexPos;
    vertexPos += lightPos;
    gl_Position = VeilCamera.ProjMat * VeilCamera.ViewMat * vec4(vertexPos - VeilCamera.CameraPosition, 1.0);

    lightMat = LightMatrix;
    invLightMat = inverse(LightMatrix);
    lightColor = Color;
    size = Size;
    maxAngle = Angle;
    maxDistance = Distance;
    occluded = Occluded;
    #ifdef INSCATTERING
    inscattering = Inscattering;
    #endif
}
