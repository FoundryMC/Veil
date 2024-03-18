#include veil:deferred_buffers

uniform vec4 ColorModulator;

in vec4 vertexColor;

void main() {
    fragAlbedo = vertexColor * ColorModulator;
}
