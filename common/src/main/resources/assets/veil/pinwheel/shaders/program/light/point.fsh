#include veil:common
#include veil:space_helper
#include veil:color_utilities
#include veil:light
#include veil:voxel_shadow

in vec3 lightPos;
in vec3 lightColor;
in float radius;
in float occluded;

uniform sampler2D AlbedoSampler;
uniform sampler2D NormalSampler;
uniform sampler2D DepthSampler;

uniform vec2 ScreenSize;

out vec4 fragColor;

const int steps = 64;
const float strength = 0.4f;

vec3 volumetric(vec3 camPos, vec3 fragPos) {
    vec3 dir = fragPos - camPos;
    float dirLength = length(dir);
    float stepSize = dirLength / float(steps);

    float jitter = fract(sin(dot(fragPos.xy, vec2(12.9898, 78.233))) * 43758.5453) - 0.5;
    vec3 scatter = vec3(0.0);
    vec3 p = camPos + jitter * 0.1;
    for (int s = 0; s < steps; s++) {
        p += normalize(dir) * stepSize;

        float d = distance(p, lightPos);
        if (d > radius) continue;
        scatter += attenuate_no_cusp(length(lightPos - p), radius) * lightColor;
    }

    return clamp(scatter / float(steps) * strength, vec3(0.0), vec3(1.0));
}

void main() {
    vec2 screenUv = gl_FragCoord.xy / ScreenSize;

    vec4 albedoColor = texture(AlbedoSampler, screenUv);
    if (albedoColor.a == 0) {
        discard;
    }

    float depth = texture(DepthSampler, screenUv).r;
    vec3 pos = screenToWorldSpace(screenUv, depth).xyz;

    // lighting calculation
    vec3 offset = lightPos - pos;

    vec3 normalVS = texture(NormalSampler, screenUv).xyz;
    vec3 lightDirection = normalize((VeilCamera.ViewMat * vec4(offset, 0.0)).xyz);
    float diffuse = clamp(dot(normalVS, lightDirection), 0.0, 1.0);
    diffuse = (diffuse + MINECRAFT_AMBIENT_LIGHT) / (1.0 + MINECRAFT_AMBIENT_LIGHT);
    diffuse *= attenuate_no_cusp(length(offset), radius);

    //vec3 volu = volumetric(VeilCamera.CameraPosition, pos);
    if (occluded > 0.5) {
        vec3 normalWS = normalize((VeilCamera.IViewMat * vec4(normalVS, 0.0)).xyz);
        float shadow = voxelshadowVisibility(pos + normalWS * 0.01, lightPos);
        diffuse *= shadow;
        //volu *= shadow;

    }

    float reflectivity = 0.05;
    vec3 diffuseColor = diffuse * lightColor;
    fragColor = vec4(albedoColor.rgb * diffuseColor * (1.0 - reflectivity) + diffuseColor * reflectivity, 1.0);
}
