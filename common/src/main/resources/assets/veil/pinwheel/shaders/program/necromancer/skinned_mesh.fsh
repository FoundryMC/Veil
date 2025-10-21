#include veil:fog

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec3 normal;

out vec4 fragColor;

void main() {
    vec4 baseColor = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;

    // #veil:albedo
    vec4 albedoColor = mix(vec4(overlayColor.rgb, baseColor.a), baseColor, overlayColor.a);

    vec4 color = albedoColor * lightMapColor;
    if (color.a < 0.05) discard;

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}