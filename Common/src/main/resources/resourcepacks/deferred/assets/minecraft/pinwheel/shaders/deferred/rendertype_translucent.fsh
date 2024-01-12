#include veil:material

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord2;
in vec4 lightmapColor;
in vec3 normal;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 fragAlbedo;
layout(location = 2) out vec4 fragNormal;
layout(location = 3) out vec4 fragMaterial;
layout(location = 4) out vec4 fragVanillaLight;
layout(location = 5) out vec4 fragLightMap;
#ifdef USE_BAKED_TRANSPARENT_LIGHTMAPS
layout(location = 6) out vec4 fragAlbedoLightMap;
#endif

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    fragColor = vec4(0.0);
    fragAlbedo = color;
    fragNormal = vec4(normal, 1.0);
    fragMaterial = vec4(BLOCK_TRANSLUCENT, 0.0, 0.0, 1.0);
    fragVanillaLight = vec4(texCoord2, 0.0, 1.0);
    fragLightMap = lightmapColor;
    #ifdef USE_BAKED_TRANSPARENT_LIGHTMAPS
    fragAlbedoLightMap = color * lightmapColor;
    #endif
}
