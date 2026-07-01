#include veil:common
#include veil:space_helper
#include veil:color_utilities
#include veil:light
#include veil:voxel_shadow

in mat4 lightMat;
in mat4 inverseLightMat;
in vec3 lightColor;
in vec2 size;
in float maxAngle;
in float maxDistance;
in float occluded;
in float volumetric;
in vec3 lightOrigin;

uniform sampler2D AlbedoSampler;
uniform sampler2D NormalSampler;
uniform sampler2D WorldDepthSampler;
uniform sampler2D HandDepthSampler;

uniform vec2 ScreenSize;

out vec4 fragColor;

// acos approximation
// faster and also doesn't flicker weirdly
float sacos(float x)
{
    float y = abs(clamp(x, -1.0, 1.0));
    float z = (-0.168577*y + 1.56723) * sqrt(1.0 - y);
    return mix(0.5*3.1415927, z, sign(x));
}

struct AreaLightResult { vec3 position; float angle; };
AreaLightResult closestPointOnPlaneAndAngle(vec3 point, mat4 planeMatrix, mat4 invPlaneMatrix, vec2 planeSize) {
    // no idea why i need to do this
    planeMatrix[3].xyz *= -1.0;
    invPlaneMatrix[3].xyz *= -1.0;
    // transform the point to the plane's local space
    vec3 localSpacePoint = (planeMatrix * vec4(point, 1.0)).xyz;
    // clamp position
    vec3 localSpacePointOnPlane = vec3(clamp(localSpacePoint.xy, -planeSize, planeSize), 0);

    // calculate the angles
    vec3 direction = normalize(localSpacePoint - localSpacePointOnPlane);
    float angle = sacos(dot(direction, vec3(0.0, 0.0, 1.0)));

    // transform back to global space
    return AreaLightResult((invPlaneMatrix * vec4(localSpacePointOnPlane, 1.0)).xyz, angle);
}

const int steps = 100;

vec3 raymarch_inscattering_fixeddist(vec3 camPos, vec3 fragPos, float depth) {
    int raymarchSteps = steps;
    float jitterStrength = 0.05;
    vec3 dir = fragPos - camPos;
    float dirLength = length(dir);
    float stepSize = maxDistance * 0.1;

    float jitter = fract(sin(dot(fragPos.xy, vec2(12.9898, 78.233))) * 43758.5453) - 0.5;
    vec3 scatter = vec3(0.0);
    vec3 p = camPos + jitter * jitterStrength;
    float distanceTraveled = 0.0;
    for (int s = 0; s < raymarchSteps; s++) {
        p += normalize(dir) * stepSize;
        distanceTraveled += stepSize;

        if (distanceTraveled > depth) break;

        float d = distance(p, lightOrigin);
        if (d > maxDistance) {
            if (length(scatter) > 0.0) break;
            continue;
        }

        AreaLightResult areaLightInfo = closestPointOnPlaneAndAngle(p, lightMat, inverseLightMat, size);
        vec3 lightPos = areaLightInfo.position;
        float angle = areaLightInfo.angle;

        float angleFalloff = clamp(angle, 0.0, maxAngle) / maxAngle;
        angleFalloff = smoothstep(1.0, 0.0, angleFalloff);

        vec3 a = attenuate_no_cusp(length(lightPos - p), maxDistance) * lightColor * angleFalloff;

        if (abs(distanceTraveled - depth) < 2.0) {
            a *= smoothstep(0.0, 1.0, abs(distanceTraveled - depth) / 2.0);
        }
        if (length(a) <= 0.0 && length(scatter) > 0.0) break;
        scatter += a;
    }

    return clamp(scatter / float(raymarchSteps) * volumetric, vec3(0.0), vec3(1.0));
}

void main() {
    vec2 screenUv = gl_FragCoord.xy / ScreenSize;

    vec4 albedoColor = texture(AlbedoSampler, screenUv);
    if (albedoColor.a == 0) {
        //discard;
    }

    vec3 normalVS = texture(NormalSampler, screenUv).xyz;
    float depth = min(texture(WorldDepthSampler, screenUv).r, texture(HandDepthSampler, screenUv).r);
    vec3 pos = screenToWorldSpace(screenUv, depth).xyz;

    // lighting calculation
    AreaLightResult areaLightInfo = closestPointOnPlaneAndAngle(pos, lightMat, inverseLightMat, size);
    vec3 lightPos = areaLightInfo.position;
    float angle = areaLightInfo.angle;

    vec3 offset = lightPos - pos;
    vec3 lightDirection = normalize((VeilCamera.ViewMat * vec4(offset, 0.0)).xyz);
    float diffuse = (dot(normalVS, lightDirection) + 1.0) * 0.5;
    diffuse = (diffuse + MINECRAFT_AMBIENT_LIGHT) / (1.0 + MINECRAFT_AMBIENT_LIGHT);
    diffuse *= attenuate_no_cusp(length(offset), maxDistance);
    // angle falloff
    float angleFalloff = clamp(angle, 0.0, maxAngle) / maxAngle;
    angleFalloff = smoothstep(1.0, 0.0, angleFalloff);
    diffuse *= angleFalloff;
    if (occluded > 0.5) {
        vec3 normalWS = normalize((VeilCamera.IViewMat * vec4(normalVS, 0.0)).xyz);
        diffuse *= voxelshadowVisibility(pos + normalWS * 0.01, lightPos);
    }
    vec3 scatter = vec3(0.0);
    if (volumetric > 0.0) {
        scatter = raymarch_inscattering_fixeddist(VeilCamera.CameraPosition, pos, linearize_depth(depth));
    }

    float reflectivity = 0.05;
    vec3 diffuseColor = diffuse * lightColor;

    fragColor = vec4(albedoColor.rgb * diffuseColor * (1.0 - reflectivity) + diffuseColor * reflectivity + scatter, 1.0);
}
