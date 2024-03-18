#include veil:color_utilities
#include veil:blend

in vec2 texCoord;

uniform sampler2D AlbedoSampler;
uniform sampler2D CompatibilitySampler;
uniform sampler2D LightSampler;

out vec4 fragColor;

void main() {
    float albedoAlpha = texture(AlbedoSampler, texCoord).a;
    vec4 diffuse = texture(LightSampler, texCoord);
    vec4 compatibility = texture(CompatibilitySampler, texCoord);
    diffuse.rgb /= diffuse.a;
    fragColor = vec4(blend(vec4(diffuse.rgb, albedoAlpha), compatibility), albedoAlpha + compatibility.a * (1.0 - albedoAlpha));
}
