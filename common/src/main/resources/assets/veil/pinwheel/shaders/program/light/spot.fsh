#include veil:common
#include veil:space_helper
#include veil:color_utilities
#include veil:light
#include veil:voxel_shadow

in mat4 lightMat;
in vec3 lightColor;
in float size;
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

struct SpotLightResult { vec3 position; float angle; };
SpotLightResult spotLightPositionAndAngle(vec3 point, mat4 lightMatrix) {
    // no idea why i need to do this
    lightMatrix[3].xyz *= -1.0;

    vec3 localSpacePoint = (lightMatrix * vec4(point, 1.0)).xyz;
    vec3 localDir = normalize(localSpacePoint);
    float angle = sacos(localDir.z);

    vec3 worldPos = (inverse(lightMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz;
    return SpotLightResult(worldPos, angle);
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
    SpotLightResult spotLightInfo = spotLightPositionAndAngle(pos, lightMat);
    vec3 lightPos = spotLightInfo.position;

    vec3 offset = lightPos - pos;
    vec3 lightDirection = normalize((VeilCamera.ViewMat * vec4(offset, 0.0)).xyz);
    float diffuse = (dot(normalVS, lightDirection) + 1.0) * 0.5;
    diffuse = (diffuse + MINECRAFT_AMBIENT_LIGHT) / (1.0 + MINECRAFT_AMBIENT_LIGHT);
    diffuse *= attenuate_no_cusp(length(offset), maxDistance);

    float angleFalloff = smoothstep(size, size - maxAngle, spotLightInfo.angle);
    diffuse *= angleFalloff;

    if (occluded > 0.5) {
        vec3 normalWS = normalize((VeilCamera.IViewMat * vec4(normalVS, 0.0)).xyz);
        diffuse *= voxelshadowVisibility(pos + normalWS * 0.01, lightPos);
    }

    float reflectivity = 0.05;
    vec3 diffuseColor = diffuse * lightColor;

    fragColor = vec4(albedoColor.rgb * diffuseColor * (1.0 - reflectivity) + diffuseColor * reflectivity, 1.0);
}