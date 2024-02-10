#include veil:common
#include veil:deferred_utils
#include veil:color_utilities
#include veil:light

in vec3 lightPos;
in vec3 lightColor;
in float radius;
in float falloff;

uniform sampler2D AlbedoSampler;
uniform sampler2D NormalSampler;
uniform usampler2D MaterialSampler;
uniform sampler2D VanillaLightSampler;
uniform sampler2D DiffuseDepthSampler;

uniform vec2 ScreenSize;

out vec4 fragColor;

float attenuate_no_cusp(float distance, float radius, float falloff)
{
    float s = distance / radius;

    if (s >= 1.0) {
        return 0.0;
    }

    float s2 = s * s;
    return (1 - s2) * (1 - s2) / (1 + falloff * s2);
}

void main() {
    vec2 screenUv = gl_FragCoord.xy / ScreenSize;

    // sample buffers
    float depth = texture(DiffuseDepthSampler, screenUv).r;
    vec3 pos = viewToWorldSpace(viewPosFromDepth(depth, screenUv));

    // lighting calculation
    vec3 offset = lightPos - pos;

    float attenuation = attenuate_no_cusp(length(offset), radius, falloff);
    if (attenuation <= 0) {
        discard;
    }

    vec3 normalVS = texture(NormalSampler, screenUv).xyz;
    vec3 lightDirection = (VeilCamera.ViewMat * vec4(normalize(offset), 0.0)).xyz;
    float diffuse = dot(normalVS, lightDirection);
    diffuse = max(MINECRAFT_AMBIENT_LIGHT, diffuse);
    diffuse *= attenuation;

    fragColor = vec4(diffuse * lightColor, 1.0);
}
