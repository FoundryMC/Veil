#version 330

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform mat3 NormalMat;

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoord2;
out vec3 normal;

void main() {
    vec3 pos = Position + ChunkOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    vertexColor = Color;
    texCoord0 = UV0;
    texCoord2 = vec2(UV2 / 256.0);
    normal = NormalMat * Normal;
}
