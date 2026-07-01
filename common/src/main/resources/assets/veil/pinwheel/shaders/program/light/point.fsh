#include veil:common
#include veil:space_helper
#include veil:color_utilities
#include veil:light
#include veil:voxel_shadow

in vec3 lightPos;
in vec3 lightColor;
in float radius;
in float occluded;
in float volumetric;

uniform sampler2D AlbedoSampler;
uniform sampler2D NormalSampler;
uniform sampler2D WorldDepthSampler;
uniform sampler2D HandDepthSampler;

uniform vec2 ScreenSize;

out vec4 fragColor;

const int steps = 50;
const float strength = 0.4f;

vec3 raymarch_inscattering_fixeddist(vec3 camPos, vec3 fragPos, float depth, bool occlude, vec3 normal) {
    int raymarchSteps = steps;
    float jitterStrength = 0.25 ;
    vec3 dir = fragPos - camPos;
    float dirLength = length(dir);
    float stepSize = 1.0;
    if (occlude) {
        //raymarchSteps /= 4;
        //stepSize *= 4;
    }

    float jitter = fract(sin(dot(fragPos.xy, vec2(12.9898, 78.233))) * 43758.5453) - 0.5;
    vec3 scatter = vec3(0.0);
    vec3 p = camPos + jitter * jitterStrength;
    float distanceTraveled = 0.0;
    for (int s = 0; s < raymarchSteps; s++) {
        p += normalize(dir) * stepSize;
        distanceTraveled += stepSize;

        float d = distance(p, lightPos);
        if (d > radius) continue;
        vec3 a = attenuate_no_cusp(length(lightPos - p), radius) * lightColor;

        if (occlude) {
            a *= voxelshadowVisibility(p + normal, lightPos);
        }

        if (abs(distanceTraveled - depth) < 10.0) {
            a *= smoothstep(0.0, 1.0, abs(distanceTraveled - depth) / 10);
        }
        scatter += a;

        if (distanceTraveled > depth) break;
    }

    return clamp(scatter / float(raymarchSteps) * strength * volumetric, vec3(0.0), vec3(1.0));
}

void main() {
    vec2 screenUv = gl_FragCoord.xy / ScreenSize;

    float depth = min(texture(WorldDepthSampler, screenUv).r, texture(HandDepthSampler, screenUv).r);
    vec3 pos = screenToWorldSpace(screenUv, depth).xyz;

    vec4 albedoColor = texture(AlbedoSampler, screenUv);
    if (albedoColor.a == 0) {
        //discard;
    }

    // lighting calculation
    vec3 offset = lightPos - pos;

    vec3 normalVS = texture(NormalSampler, screenUv).xyz;
    vec3 lightDirection = normalize((VeilCamera.ViewMat * vec4(offset, 0.0)).xyz);
    float diffuse = clamp(dot(normalVS, lightDirection), 0.0, 1.0);
    diffuse = (diffuse + MINECRAFT_AMBIENT_LIGHT) / (1.0 + MINECRAFT_AMBIENT_LIGHT);
    diffuse *= attenuate_no_cusp(length(offset), radius);

    vec3 normalWS;
    if (occluded > 0.5) {
        normalWS = normalize((VeilCamera.IViewMat * vec4(normalVS, 0.0)).xyz);
        float shadow = voxelshadowVisibility(pos + normalWS * 0.01, lightPos);
        diffuse *= shadow;
    }
    vec3 scatter = vec3(0.0);
    if (volumetric > 0.0) {
        scatter = raymarch_inscattering_fixeddist(VeilCamera.CameraPosition + VeilCamera.CameraBobOffset, pos, linearize_depth(depth), occluded > 0.5, normalWS * 0.1);
    }

    float reflectivity = 0.05;
    vec3 diffuseColor = diffuse * lightColor;
    fragColor = vec4(albedoColor.rgb * diffuseColor * (1.0 - reflectivity) + diffuseColor * reflectivity + scatter, 1.0);
}
