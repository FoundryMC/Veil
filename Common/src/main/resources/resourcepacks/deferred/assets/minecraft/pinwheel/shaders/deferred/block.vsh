#version 330

#include veil:light

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV2;
layout(location = 4) in vec3 Normal;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform mat3 NormalMat;

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoord2;
out vec4 lightmapColor;
out vec3 normal;

void main() {
    vec3 pos = Position + ChunkOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    vertexColor = Color;
    texCoord0 = UV0;
    texCoord2 = minecraft_sample_lightmap_coords(UV2);
    lightmapColor = texture(Sampler2, texCoord2);
    normal = NormalMat * Normal;
}
