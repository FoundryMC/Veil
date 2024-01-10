#version 330

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord2;
in vec3 normal;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 fragAlbedo;
layout(location = 2) out vec4 fragNormal;
layout(location = 3) out vec4 fragMaterial;
layout(location = 4) out vec4 fragEmissive;
layout(location = 5) out vec4 fragVanillaLight;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < vertexColor.a) {
        discard;
    }
    fragColor = vec4(0.0);
    fragAlbedo = color;
    fragNormal = vec4(normal, 0.0);
    fragMaterial = vec4(0.0);
    fragEmissive = vec4(0.0);
    fragVanillaLight = vec4(texCoord2, 0.0, 0.0);
}
