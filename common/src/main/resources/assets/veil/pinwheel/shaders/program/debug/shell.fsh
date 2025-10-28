uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec3 vertexNormal;

out vec4 fragColor;

void main() {
    fragColor = vec4(abs(vertexNormal), 1.0) * ColorModulator;
}
