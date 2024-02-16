#include veil:translucent_buffers

uniform vec4 ColorModulator;

in vec4 vertexColor;

void main() {
    fragAlbedo = vertexColor * ColorModulator;
    #ifdef USE_BAKED_TRANSPARENT_LIGHTMAPS
    fragAlbedoLightMap = fragAlbedo;
    #endif
}
