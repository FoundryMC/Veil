#version 330

flat in vec4 color;
in vec2 texCoord2;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 fragAlbedo;
layout(location = 2) out vec4 fragNormal;
layout(location = 3) out vec4 fragMaterial;
layout(location = 4) out vec4 fragEmissive;
layout(location = 5) out vec4 fragVanillaLight;

void main() {
    fragColor = vec4(0.0);
    fragAlbedo = color;
    fragNormal = vec4(0.0, 1.0, 0.0, 0.0);
    fragMaterial = vec4(0.0);
    fragEmissive = vec4(0.0);
    fragVanillaLight = vec4(texCoord2, 0.0, 0.0);
}
