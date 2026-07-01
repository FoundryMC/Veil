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

const int steps = 100;

vec3 raymarch_inscattering_fixeddist(vec3 camPos, vec3 fragPos, float depth) {
    int raymarchSteps = steps;
    float jitterStrength = 0.25;
    vec3 dir = fragPos - camPos;
    float dirLength = length(dir);
    // optimization: only sample as much as we need
    float stepSize = radius * 0.25;

    float jitter = fract(sin(dot(fragPos.xy, vec2(12.9898, 78.233))) * 43758.5453) - 0.5;
    vec3 scatter = vec3(0.0);
    vec3 p = camPos + jitter * jitterStrength;
    float distanceTraveled = 0.0;
    for (int s = 0; s < raymarchSteps; s++) {
        p += normalize(dir) * stepSize;
        distanceTraveled += stepSize;

        if (distanceTraveled > depth) break;

        float d = distance(p, lightPos);
        if (d > radius) {
            // optimization: if scattering has been accumulated, and we're outside the radius
            // we've likely exited the volume from the other side; exit light calculations
            if (length(scatter) > 0.0) break;
            continue;
        }
        vec3 a = attenuate_no_cusp(length(lightPos - p), radius) * lightColor;

        if (abs(distanceTraveled - depth) < 10.0) {
            a *= smoothstep(0.0, 1.0, abs(distanceTraveled - depth) / 10);
        }
        scatter += a;
    }

    return clamp(scatter / float(raymarchSteps) * volumetric, vec3(0.0), vec3(1.0));
}

void main() {
    vec2 screenUv = gl_FragCoord.xy / ScreenSize;

    float depth = min(texture(WorldDepthSampler, screenUv).r, texture(HandDepthSampler, screenUv).r);
    vec3 pos = screenToWorldSpace(screenUv, depth).xyz;

    vec4 albedoColor = texture(AlbedoSampler, screenUv);

    // lighting calculation
    vec3 offset = lightPos - pos;

    vec3 normalVS = texture(NormalSampler, screenUv).xyz;
    vec3 lightDirection = normalize((VeilCamera.ViewMat * vec4(offset, 0.0)).xyz);
    float diffuse = clamp(dot(normalVS, lightDirection), 0.0, 1.0);
    diffuse = (diffuse + MINECRAFT_AMBIENT_LIGHT) / (1.0 + MINECRAFT_AMBIENT_LIGHT);
    diffuse *= attenuate_no_cusp(length(offset), radius);

    if (occluded > 0.5) {
        vec3 normalWS = normalize((VeilCamera.IViewMat * vec4(normalVS, 0.0)).xyz);
        float shadow = voxelshadowVisibility(pos + normalWS * 0.01, lightPos);
        diffuse *= shadow;
    }
    vec3 scatter = vec3(0.0);
    if (volumetric > 0.0 && occluded < 0.5) {
        scatter = raymarch_inscattering_fixeddist(VeilCamera.CameraPosition + VeilCamera.CameraBobOffset, pos, linearize_depth(depth));
    }

    float reflectivity = 0.05;
    vec3 diffuseColor = diffuse * lightColor;
    fragColor = vec4(albedoColor.rgb * diffuseColor * (1.0 - reflectivity) + diffuseColor * reflectivity + scatter, 1.0);
}
