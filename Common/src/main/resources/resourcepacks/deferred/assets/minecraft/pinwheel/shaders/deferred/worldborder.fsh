#include veil:material
#include veil:translucent_buffers
#include veil:blend

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec3 normal;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * ColorModulator;
    if (color.a == 0.0) {
        discard;
    }
    fragAlbedo = color;
    fragNormal = vec4(normal, 1.0);
    fragMaterial = vec4(WORLD_BORDER, ADDITIVE_TRANSPARENCY, 0.0, 1.0);
    fragLightSampler = vec4(0.0, 1.0, 0.0, 1.0);
    fragLightMap = vec4(1.0);
    #ifdef USE_BAKED_TRANSPARENT_LIGHTMAPS
    fragAlbedoLightMap = color;
    #endif
}
