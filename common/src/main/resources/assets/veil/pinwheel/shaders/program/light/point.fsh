#include veil:common
#include veil:space_helper
#include veil:color_utilities
#include veil:light
#include veil:voxel_shadow

in vec3 lightPos;
in vec3 lightColor;
in float radius;
in float occluded;
in float inscattering;

uniform sampler2D AlbedoSampler;
uniform sampler2D NormalSampler;
uniform sampler2D DepthSampler;

uniform vec2 ScreenSize;

out vec4 fragColor;

//Fog density
#define DENSITY 2.6
//Surface pass rate
#define PASSTHROUGH 0.5

#define STEPS 10.0

float sdSphere( vec3 p, float r )
{
    return (length(p - lightPos) - r) / DENSITY + PASSTHROUGH;
}

float random(vec2 uv) {
    return fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 ray(vec3 dir, vec3 pos) {
    //Output brightness
    #define BRIGHTNESS 0.004

    //Accumulative color
    vec3 col = vec3(0.0);

    //Glow raymarch loop
    for(float i = 0.0; i<STEPS; i++)
    {
        //Glow density
        float vol = sdSphere(pos, 1.);
        vec3 offset = lightPos - pos;
        float atten = attenuate_no_cusp(length(offset), radius);
        //Step forward
        pos += dir * vol;

        //Add the sample color
        col += (lightColor * inscattering * atten) / vol;
    }
    //Tanh tonemapping
    //https://mini.gmshaders.com/p/tonemaps
    col = tanh(BRIGHTNESS * col);

    return col;
}

void main() {
    vec2 screenUv = gl_FragCoord.xy / ScreenSize;

    vec4 albedoColor = texture(AlbedoSampler, screenUv);
    float diffuse = 0;
    if (albedoColor.a > 0) {
        float depth = texture(DepthSampler, screenUv).r;
        vec3 pos = screenToWorldSpace(screenUv, depth).xyz;

        // lighting calculation
        vec3 offset = lightPos - pos;

        vec3 normalVS = texture(NormalSampler, screenUv).xyz;
        vec3 lightDirection = normalize((VeilCamera.ViewMat * vec4(offset, 0.0)).xyz);
        diffuse = clamp(dot(normalVS, lightDirection), 0.0, 1.0);
        diffuse = (diffuse + MINECRAFT_AMBIENT_LIGHT) / (1.0 + MINECRAFT_AMBIENT_LIGHT);
        diffuse *= attenuate_no_cusp(length(offset), radius);
        if (occluded > 0.5) {
            vec3 normalWS = normalize((VeilCamera.IViewMat * vec4(normalVS, 0.0)).xyz);
            diffuse *= voxelshadowVisibility(pos + normalWS * 0.01, lightPos);
        }
    }
    vec3 volume = vec3(0);
    if (inscattering > 0.0) {
        volume = ray(viewDirFromUv(screenUv), VeilCamera.CameraPosition);
    }

    float reflectivity = 0.05;
    vec3 diffuseColor = diffuse * lightColor;
    fragColor = vec4(albedoColor.rgb * diffuseColor * (1.0 - reflectivity) + diffuseColor * reflectivity + volume, 1.0);
}
