uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec2 texCoord0;
in vec4 vertexColor;
in vec4 vertexNormal;

out vec4 fragColor;

void main() {
    fragColor = vertexNormal * ColorModulator;
}
