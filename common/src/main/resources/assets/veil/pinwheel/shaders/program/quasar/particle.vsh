#include veil:fog
#veil:buffer veil:camera VeilCamera

layout(location = 0) in vec3 Position;
layout(location = 1) in vec3 Normal;
layout(location = 2) in mat4 ParticleMat;
layout(location = 6) in float Scale;
layout(location = 7) in vec2 UV0Min;
layout(location = 8) in vec2 UV0Max;
layout(location = 9) in vec4 Color;
layout(location = 10) in ivec2 UV2;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
#ifdef VEIL_NORMAL
uniform mat3 NormalMat;
#endif

out float vertexDistance;
out vec2 texCoord0;
out vec4 vertexColor;
out vec4 lightmapColor;

void main() {
    vec4 WorldPosition = ModelViewMat * ParticleMat * vec4(Position * Scale, 1.0);
    gl_Position = ProjMat * WorldPosition;
    vertexDistance = length(WorldPosition.xyz);
    vec2 uvs[4];
    uvs[0] = UV0Min,
    uvs[1] = vec2(UV0Min.x, UV0Max.y),
    uvs[2] = UV0Max,
    uvs[3] = vec2(UV0Max.x, UV0Min.y);
    texCoord0 = uvs[gl_VertexID % 4];
    #ifdef VEIL_LIGHT_UV
    // #veil:light_uv
    vec2 texCoord2 = vec2(UV2 / 256.0);
    #endif
    vertexColor = Color;
    lightmapColor = texelFetch(Sampler2, UV2 / 16, 0);
    #ifdef VEIL_NORMAL
    // #veil:normal
    vec3 normal = NormalMat * Normal;
    #endif
}

