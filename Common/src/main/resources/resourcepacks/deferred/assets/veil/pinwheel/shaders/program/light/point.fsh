#include veil:common
#include veil:deferred_utils
#include veil:color_utilities
#include veil:light

in vec3 lightPos;
in vec3 lightColor;
in float radius;

uniform sampler2D AlbedoSampler;
uniform sampler2D NormalSampler;
uniform usampler2D MaterialSampler;
uniform sampler2D VanillaLightSampler;
uniform sampler2D DiffuseDepthSampler;

uniform vec2 ScreenSize;

out vec4 fragColor;

void main() {
    vec2 screenUv = gl_FragCoord.xy / ScreenSize;

    // sample buffers
    float depth = texture(DiffuseDepthSampler, screenUv).r;
    vec3 pos = viewToWorldSpace(viewPosFromDepth(depth, screenUv));

    // lighting calculation
    vec3 offset = lightPos - pos;

    float attenuation = attenuate_no_cusp(length(offset), radius);
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
