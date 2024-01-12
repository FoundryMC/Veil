#include veil:material
#include veil:translucent_buffers

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
    fragColor = vec4(0.0);
    fragAlbedo = color;
    fragNormal = vec4(0.0, 0.0, 1.0, 1.0);
    fragMaterial = vec4(PARTICLE, 0.0, 0.0, 1.0);
    fragLightSampler = vec4(texCoord2, 0.0, 1.0);
    fragLightMap = lightmapColor;
    #ifdef USE_BAKED_TRANSPARENT_LIGHTMAPS
    fragAlbedoLightMap = color * lightmapColor;
    #endif
}