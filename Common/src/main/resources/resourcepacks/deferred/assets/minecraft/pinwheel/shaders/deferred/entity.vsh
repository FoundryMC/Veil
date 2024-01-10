#version 330

#include veil:light

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV1;
layout(location = 4) in ivec2 UV2;
layout(location = 5) in vec3 Normal;

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 NormalMat;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoord2;
out vec4 overlayColor;
out vec3 normal;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    #ifdef DISABLE_VANILLA_ENTITY_LIGHT
    vertexColor = Color;
    #else
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color);
    #endif
    texCoord0 = UV0;
    texCoord2 = vec2(UV2 / 16.0);
    overlayColor = texelFetch(Sampler1, UV1, 0);
    normal = NormalMat * Normal;
}
