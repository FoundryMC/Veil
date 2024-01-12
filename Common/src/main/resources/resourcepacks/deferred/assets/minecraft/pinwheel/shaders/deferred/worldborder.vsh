layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 NormalMat;

out vec2 texCoord0;
out vec3 normal;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    normal = NormalMat * vec3(0.0, 1.0, 0.0);
}
