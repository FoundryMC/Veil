
uniform mat4 ModelToWorld;
uniform mat4 IModelToWorld;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in vec2 UV2;
layout(location = 4) in vec3 Normal;

out vec2 texCoord0;
out vec4 vertexColor;
out vec3 vertexNormal;

out vec2 texCoord1;

void main() {
    vec4 pos = vec4(Position, 1.0);
    gl_Position = ProjMat * ModelViewMat * pos;

    vertexNormal = Normal;
    texCoord0 = UV0;
    texCoord1 = UV0;
    texCoord1.x += fract(5.0*Normal.x+3.1415926535*Normal.z);
    vertexColor = Color;
}
