#include veil:color_utilities

in vec2 screenUv;

uniform sampler2D CompatibilitySampler;
uniform sampler2D AlbedoSampler;
uniform sampler2D LightSampler; // Light image

out vec4 fragColor;

void main() {
    vec4 albedo = texture(AlbedoSampler, screenUv);
    vec4 compatibility = texture(CompatibilitySampler, screenUv);
    vec3 light = texture(LightSampler, screenUv).rgb;
    fragColor = vec4(albedo.rgb * light, albedo.a);
    fragColor.rgb = mix(fragColor.rgb, compatibility.rgb, compatibility.a);
}
