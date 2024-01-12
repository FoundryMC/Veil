flat in vec4 color;
in vec2 texCoord2;
in vec4 lightmapColor;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 fragAlbedo;
layout(location = 2) out vec4 fragNormal;
layout(location = 3) out vec4 fragMaterial;
layout(location = 4) out vec4 fragLightSampler;
layout(location = 5) out vec4 fragLightMap;

void main() {
    fragColor = vec4(0.0);
    fragAlbedo = color;
    fragNormal = vec4(0.0, 1.0, 0.0, 1.0);
    fragMaterial = vec4(0.0);
    fragLightSampler = vec4(texCoord2, 0.0, 1.0);
    fragLightMap = lightmapColor;
}
