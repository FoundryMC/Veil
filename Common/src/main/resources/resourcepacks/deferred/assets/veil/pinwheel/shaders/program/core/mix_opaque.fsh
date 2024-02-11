#include veil:color_utilities
#include veil:blend

in vec2 texCoord;

uniform sampler2D CompatibilitySampler;
uniform sampler2D AlbedoSampler;
uniform sampler2D LightSampler;

out vec4 fragColor;

void main() {
    vec3 albedo = texture(AlbedoSampler, texCoord).rgb;
    vec4 compatibility = texture(CompatibilitySampler, texCoord);
    vec3 light = texture(LightSampler, texCoord).rgb;
    fragColor = vec4(albedo * light, 1.0);
    fragColor.rgb = blend(fragColor, compatibility);
    fragColor.a += compatibility.a * (1.0 - fragColor.a);
}
