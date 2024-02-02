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
    //idk why i have to do this seperately
    vec3 lightPos = planeMatrix[3].xyz;

    // transform the point to the plane's local space
    vec3 localPoint = (vec4(point, 1.0) * inverse(planeMatrix)).xyz - lightPos;
    // clamp position
    vec3 pointOnPlane = vec3(clamp(localPoint.xy, -planeSize, planeSize), 0);

    vec3 direction = normalize(localPoint - pointOnPlane);
    float angle = sacos(dot(direction, vec3(0, 0, 1)));

    // transform back to global space
    return AreaLightResult((vec4(pointOnPlane, 1.0) * planeMatrix).xyz + lightPos, angle);
}

//float sBox( vec3 ro, vec3 rd, mat4 txx, vec3 rad )
//{
//    txx[3].xyz *= -1.0;
//    vec3 rdd = (txx*vec4(rd,0.0)).xyz;
//    vec3 roo = (txx*vec4(ro,1.0)).xyz;
//
//    vec3 m = 1.0/rdd;
//    vec3 n = m*roo;
//    vec3 k = abs(m)*rad;
//
//    vec3 t1 = -n - k;
//    vec3 t2 = -n + k;
//
//    float tN = max( max( t1.x, t1.y ), t1.z );
//    float tF = min( min( t2.x, t2.y ), t2.z );
//    if( tN > tF || tF < 0.0) return -1.0;
//
//    return tN;
//}

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

//    //draw plane
//    float distToPlane = sBox(VeilCamera.CameraPosition, viewDirFromUv(screenUv), lightMat, vec3(size, 0.0));
//    float planeLight = 0.0;
//    if (distToPlane > 0 && distToPlane < length(viewPos)) planeLight = 1000.0;

    fragColor = vec4(diffuse * lightColor, 1.0);
}

