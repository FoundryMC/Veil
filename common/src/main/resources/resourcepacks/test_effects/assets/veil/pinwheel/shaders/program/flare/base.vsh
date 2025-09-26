layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in vec2 UV2;
layout(location = 4) in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 NormalMat;

out vec2 texCoord0;
out vec4 vertexColor;
out vec3 vertexNormal;

void main() {
    vec4 pos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * pos;

    vertexNormal = Normal;
    texCoord0 = UV0;
    vertexColor = Color;
}
