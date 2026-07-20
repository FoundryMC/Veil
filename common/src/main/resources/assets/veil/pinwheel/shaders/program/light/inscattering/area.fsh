#include veil:common
#include veil:space_helper
#include veil:color_utilities
#include veil:light
#include veil:voxel_shadow

in mat4 lightMat;
in mat4 invLightMat;
in vec3 lightColor;
in vec2 size;
in float maxAngle;
in float maxDistance;
in float occluded;
in float inscattering;

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

//Fog density
#define DENSITY 2.6
//Surface pass rate
#define PASSTHROUGH 0.5

#define STEPS 50.0

float sdSphere( vec3 p, float r )
{
    return (length(p) - r) / DENSITY + PASSTHROUGH;
}

vec3 ray(vec3 dir, vec3 pos, vec2 uv, float depth) {
    //Output brightness
    #define BRIGHTNESS 0.004

    //Accumulative color
    vec3 jitter = vec3(random(gl_FragCoord.xy + pos.xx), random(gl_FragCoord.xy + pos.yy), random(gl_FragCoord.xy + pos.zz)) - 0.5;
    vec3 p = pos + jitter * 0.01;
    vec3 col = vec3(0.0);
    float d = 0;
    float fov = getFov();

    //Glow raymarch loop
    for(float i = 0.0; i<STEPS; i++)
    {
        //Glow density
        AreaLightResult areaLightInfo = closestPointOnPlaneAndAngle(p, lightMat, invLightMat, size);
        vec3 lightPos = areaLightInfo.position;
        float angle = areaLightInfo.angle;

        float vol = sdSphere(p - lightPos, 1.);
        d += vol - (fov * pow(2 * length(uv) - 1, 2));

        vec3 offset = lightPos - p;
        float atten = attenuate_no_cusp(length(offset), maxDistance);
        float angleFalloff = clamp(angle, 0.0, maxAngle) / maxAngle;
        angleFalloff = smoothstep(1.0, 0.0, angleFalloff);
        //Step forward
        p += dir * vol;
        if ((depth - d) < 1.0) {
            atten *= smoothstep(0.0, 1.0, (depth - d));
        }

        //Add the sample color
        col += (lightColor * inscattering * atten * angleFalloff) / vol;
    }
    //Tanh tonemapping
    //https://mini.gmshaders.com/p/tonemaps
    col = tanh(BRIGHTNESS * col);

    return col;
}

void main() {
    vec2 screenUv = gl_FragCoord.xy / (ScreenSize / 4.0);

    vec3 volume = vec3(0);
    if (inscattering > 0.0) {
        float d = linearizeDepth(texture(DepthSampler, screenUv).r);
        volume = ray(viewDirFromUv(screenUv), VeilCamera.CameraPosition + VeilCamera.CameraBobOffset, screenUv, d);
    }

    fragColor = vec4(volume, 1.0);
}
