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

uniform sampler2D DepthSampler;

uniform vec2 ScreenSize;

out vec4 fragColor;

//Fog density
#define DENSITY 2.6
//Surface pass rate
#define PASSTHROUGH 0.5

#define STEPS 50.0

float sdSphere( vec3 p, float r )
{
    return (length(p - lightPos) - r) / DENSITY + PASSTHROUGH;
}

vec3 ray(vec3 dir, vec3 pos, vec3 fragPos) {
    //Output brightness
    #define BRIGHTNESS 0.004

    //Accumulative color
    vec3 col = vec3(0.0);
    float d = 0;
    float fragDistance = distance(pos, fragPos);

    //Glow raymarch loop
    for(float i = 0.0; i<STEPS; i++)
    {
        //Glow density
        float vol = sdSphere(pos, 1.);
        vec3 offset = lightPos - pos;
        float atten = attenuate_no_cusp(length(offset), radius);
        //Step forward
        pos += dir * vol;

        d += vol;
        if (fragDistance - d < 1.0) {
            atten *= smoothstep(0.0, 1.0, fragDistance - d);
        }

        //Add the sample color
        col += (lightColor * inscattering * atten) / vol;
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
        float depth = texture(DepthSampler, screenUv).r;
        volume = ray(viewDirFromUv(screenUv), VeilCamera.CameraPosition + VeilCamera.CameraBobOffset, screenToWorldSpace(screenUv, depth).xyz);
    }

    fragColor = vec4(volume, 1.0);
}
