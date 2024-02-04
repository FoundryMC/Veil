#include veil:common
#include veil:deferred_utils
#include veil:color_utilities
#include veil:light

in mat4 lightMat;
in vec3 lightColor;
in vec2 size;
in float maxAngle;
in float maxDistance;
in float falloff;

uniform sampler2D AlbedoSampler;
uniform sampler2D NormalSampler;
uniform usampler2D MaterialSampler;
uniform sampler2D VanillaLightSampler;
uniform sampler2D DiffuseDepthSampler;

uniform vec2 ScreenSize;

out vec4 fragColor;

float attenuate_no_cusp(float distanceIn, float radiusIn, float falloffIn)
{
    float s = distanceIn / radiusIn;

    if (s >= 1.0){
        return 0.0;
    }

    float s2 = s * s;
    return (1 - s2) * (1 - s2) / (1 + falloffIn * s2);
}

// acos approximation
// faster and also doesn't flicker weirdly
float sacos( float x )
{
    float y = abs( clamp(x,-1.0,1.0) );
    float z = (-0.168577*y + 1.56723) * sqrt(1.0 - y);
    return mix( 0.5*3.1415927, z, sign(x) );
}

struct AreaLightResult { vec3 position; float angle; };
AreaLightResult closestPointOnPlaneAndAngle(vec3 point, mat4 planeMatrix, vec2 planeSize)
{
    // no idea why i need to do this
    planeMatrix[3].xyz *= -1.0;
    // transform the point to the plane's local space
    vec3 localSpacePoint = (planeMatrix * vec4(point, 1.0)).xyz;
    // clamp position
    vec3 localSpacePointOnPlane = vec3(clamp(localSpacePoint.xy, -planeSize, planeSize), 0);

    // calculate the angles
    vec3 direction = normalize(localSpacePoint - localSpacePointOnPlane);
    float angle = sacos(dot(direction, vec3(0, 0, 1)));

    // transform back to global space
    return AreaLightResult((inverse(planeMatrix) * vec4(localSpacePointOnPlane, 1.0)).xyz, angle);
}

void main() {
    vec2 screenUv = gl_FragCoord.xy / ScreenSize;

    // sample buffers
    vec3 normalVS = texture(NormalSampler, screenUv).xyz;
    float depth = texture(DiffuseDepthSampler, screenUv).r;
    vec3 viewPos = viewPosFromDepth(depth, screenUv);
    vec3 pos = viewToWorldSpace(viewPos);

    // lighting calculation
    AreaLightResult areaLightInfo = closestPointOnPlaneAndAngle(pos, lightMat, size);
    vec3 lightPos = areaLightInfo.position;
    float angle = areaLightInfo.angle;

    vec3 offset = lightPos - pos;
    vec3 lightDirection = (VeilCamera.ViewMat * vec4(normalize(offset), 0.0)).xyz;
    float diffuse = (dot(normalVS, lightDirection) + 1.0) * 0.5;

    diffuse = max(MINECRAFT_AMBIENT_LIGHT, diffuse);
    diffuse *= attenuate_no_cusp(length(offset), maxDistance, falloff);
    // angle falloff
    float angleFalloff = mapClamped(angle, 0.0, maxAngle, 1.0, 0.0);
    diffuse *= smoothstep(0.0, 1.0, angleFalloff);

    fragColor = vec4(diffuse * lightColor, 1.0);
}

