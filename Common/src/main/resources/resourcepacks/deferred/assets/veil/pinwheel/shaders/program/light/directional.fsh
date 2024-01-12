#include veil:common
#include veil:deferred_utils
#include veil:color_utilities

in vec2 texCoord;

uniform sampler2D AlbedoSampler;
uniform sampler2D NormalSampler;
uniform sampler2D MaterialSampler;
uniform sampler2D EmissiveSampler;
uniform sampler2D VanillaLightSampler;
uniform sampler2D DiffuseDepthSampler;

uniform vec3 LightColor;
//const vec3 LightColor = vec3(1.25, 1.15, 0.7);
uniform vec3 LightDirection;

out vec4 fragColor;

float rimLight(vec3 lightDir, float worldDepth, float size) {
    vec2 uv = texCoord + lightDir.xy * size * (VeilCamera.ProjMat[1][1] / -worldDepth);
    float depthSample = texture(DiffuseDepthSampler, uv).r;
    float worldDepth2 = depthSampleToWorldDepth(depthSample);
    return clamp(pow(max(worldDepth2 - worldDepth, 0.0) * 0.15, 2.0), 0.0, 1.0);
}

void main() {
    // sample buffers
    vec3 normalVS = texture(NormalSampler, texCoord).xyz;
    //vec2 lightMapUv = texture(VanillaLightSampler, screenUv).xy;
    float depthSample = texture(DiffuseDepthSampler, texCoord).r;
    float worldDepth = depthSampleToWorldDepth(depthSample);
    vec3 lightDirectionVS = worldToViewSpaceDirection(LightDirection);

    // lighting calculation
    float diffuse = -dot(normalVS, lightDirectionVS);
    diffuse = smoothstep(-0.2, 0.2, diffuse);
    float rim = rimLight(lightDirectionVS, worldDepth, 0.025);
    diffuse = max(diffuse, rim);
    float specular = 0.0;
    if (diffuse > 0.5)
    specular = rim * 0.15;

    fragColor = vec4((diffuse + specular) * LightColor, 1.0);
}
