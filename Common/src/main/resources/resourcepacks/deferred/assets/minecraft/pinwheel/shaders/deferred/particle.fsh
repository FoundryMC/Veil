#include veil:material
#include veil:translucent_buffers
#include veil:blend

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord2;
in vec4 lightmapColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    fragAlbedo = color;
    fragNormal = vec4(0.0, 0.0, 1.0, 1.0);
    fragMaterial = ivec4(PARTICLE, TRANSLUCENT_TRANSPARENCY, 0, 1);
    fragLightSampler = vec4(texCoord2, 0.0, 1.0);
    fragLightMap = lightmapColor;
    #ifdef USE_BAKED_TRANSPARENT_LIGHTMAPS
    fragAlbedoLightMap = color * lightmapColor;
    #endif
}
