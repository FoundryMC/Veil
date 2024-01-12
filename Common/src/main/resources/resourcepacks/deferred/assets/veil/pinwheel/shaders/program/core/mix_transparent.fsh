#include veil:color_utilities
#include veil:blend

in vec2 texCoord;

uniform sampler2D CompatibilitySampler;
#ifdef USE_BAKED_TRANSPARENT_LIGHTMAPS
uniform sampler2D LightMapAlbedoSampler;
#else
uniform sampler2D AlbedoSampler;
#endif
uniform sampler2D LightSampler;
uniform sampler2D DepthSampler;

out vec4 fragColor;

void main() {
    #ifdef USE_BAKED_TRANSPARENT_LIGHTMAPS
    vec4 albedo = texture(LightMapAlbedoSampler, texCoord);
    #else
    vec4 albedo = texture(AlbedoSampler, texCoord);
    #endif

    vec4 compatibility = texture(CompatibilitySampler, texCoord);
    vec3 light = texture(LightSampler, texCoord).rgb;
    fragColor = vec4(albedo.rgb * light, albedo.a);
    fragColor.rgb = blend(fragColor.rgb, compatibility);
    fragColor.a += compatibility.a * (1.0 - fragColor.a);
    gl_FragDepth = texture(DepthSampler, texCoord).r;
}
