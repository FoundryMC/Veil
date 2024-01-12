layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in ivec2 UV2;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec4 ColorModulator;

flat out vec4 color;
out vec2 texCoord2;
out vec4 lightmapColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    color = Color * ColorModulator;
    texCoord2 = UV2 / 256.0;
    lightmapColor = texture(Sampler2, texCoord2);
}
