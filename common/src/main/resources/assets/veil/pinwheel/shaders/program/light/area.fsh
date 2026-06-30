#include veil:common
#include veil:space_helper
#include veil:color_utilities
#include veil:light
#include veil:voxel_shadow

in mat4 lightMat;
in vec3 lightColor;
in vec2 size;
in float maxAngle;
in float maxDistance;
in float occluded;

uniform sampler2D AlbedoSampler;
uniform sampler2D NormalSampler;
uniform sampler2D DepthSampler;

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
AreaLightResult closestPointOnPlaneAndAngle(vec3 point, mat4 planeMatrix, vec2 planeSize) {
    // no idea why i need to do this
    planeMatrix[3].xyz *= -1.0;
    // transform the point to the plane's local space
    vec3 localSpacePoint = (planeMatrix * vec4(point, 1.0)).xyz;
    // clamp position
    vec3 localSpacePointOnPlane = vec3(clamp(localSpacePoint.xy, -planeSize, planeSize), 0);

    // calculate the angles
    vec3 direction = normalize(localSpacePoint - localSpacePointOnPlane);
    float angle = sacos(dot(direction, vec3(0.0, 0.0, 1.0)));

    // transform back to global space
    return AreaLightResult((inverse(planeMatrix) * vec4(localSpacePointOnPlane, 1.0)).xyz, angle);
}

const int steps = 64;
const float strength = 4.0f;

vec3 volumetric(vec3 camPos, vec3 fragPos, vec3 lightPos, float angle) {
    vec3 dir = fragPos - camPos;
    float dirLength = length(dir);
    float stepSize = dirLength / float(steps);

    float jitter = fract(sin(dot(fragPos.xy, vec2(12.9898, 78.233))) * 43758.5453) - 0.5;
    vec3 scatter = vec3(0.0);
    vec3 p = camPos + jitter * 0.1;
    for (int s = 0; s < steps; s++) {
        p += normalize(dir) * stepSize;

        float d = distance(p, lightPos);
        if (d > maxDistance) continue;
        float a = attenuate_no_cusp(length(lightPos - p), maxDistance);
        vec3 Lc = normalize(lightPos - p);
        float cd = dot(-Lc, normalize(lightMat[3].xyz));
        a *= clamp((cd - size.y + angle) / max(size.x - size.y, 1e-4), 0.0, 1.0);

        scatter += lightColor * a;
    }

    return clamp(scatter / float(steps) * strength, vec3(0.0), vec3(1.0));
}

void main() {
    vec2 screenUv = gl_FragCoord.xy / ScreenSize;

    vec4 albedoColor = texture(AlbedoSampler, screenUv);
    if (albedoColor.a == 0) {
        discard;
    }

    vec3 normalVS = texture(NormalSampler, screenUv).xyz;
    float depth = texture(DepthSampler, screenUv).r;
    vec3 pos = screenToWorldSpace(screenUv, depth).xyz;

    // lighting calculation
    AreaLightResult areaLightInfo = closestPointOnPlaneAndAngle(pos, lightMat, size);
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

    float reflectivity = 0.05;
    vec3 diffuseColor = diffuse * lightColor;

    vec3 volu = volumetric(VeilCamera.CameraPosition, pos, lightPos, angle);

    fragColor = vec4(albedoColor.rgb * diffuseColor * (1.0 - reflectivity) + diffuseColor * reflectivity + volu, 1.0);
}
