#include veil:common
#include veil:space_helper
#include veil:color_utilities
#include veil:light
#include veil:voxel_shadow
#ifdef INSCATTERING
#include veil:inscattering
#endif

in vec3 lightPos;
in vec3 lightColor;
in float radius;
in float occluded;
in float inscattering;

#ifndef INSCATTERING
uniform sampler2D AlbedoSampler;
uniform sampler2D NormalSampler;
#endif
uniform sampler2D DepthSampler;

uniform vec2 ScreenSize;

out vec4 fragColor;

#ifdef INSCATTERING
vec3 ray(vec3 dir, vec3 pos, vec3 fragPos) {
    //Output brightness
    #define BRIGHTNESS 0.004

    //Accumulative color
    vec3 col = vec3(0.0);
    float d = 0;
    float fragDistance = distance(pos, fragPos);

    //Glow raymarch loop
    for (float i = 0.0; i < STEPS; i++)
    {
        //Glow density
        float vol = sdSphere(pos - lightPos, 1.0);
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
    vec3 volume = vec3(0);
    if (inscattering > 0.0) {
        vec2 screenUv = gl_FragCoord.xy / (ScreenSize / 4.0);
        float depth = texture(DepthSampler, screenUv).r;
        volume = ray(viewDirFromUv(screenUv), VeilCamera.CameraPosition + VeilCamera.CameraBobOffset, screenToWorldSpace(screenUv, depth).xyz);
    }

    fragColor = vec4(volume, 1.0);
}
#else
void main() {
    vec2 screenUv = gl_FragCoord.xy / ScreenSize;

    vec4 albedoColor = texture(AlbedoSampler, screenUv);

    if (albedoColor.a == 0) {
        discard;
    }
    float depth = texture(DepthSampler, screenUv).r;
    vec3 pos = screenToWorldSpace(screenUv, depth).xyz;

    // lighting calculation
    vec3 offset = lightPos - pos;
    vec3 normalVS = texture(NormalSampler, screenUv).xyz;
    vec3 lightDirection = normalize((VeilCamera.ViewMat * vec4(offset, 0.0)).xyz);
    float diffuse = clamp(dot(normalVS, lightDirection), 0.0, 1.0);
    diffuse = (diffuse + MINECRAFT_AMBIENT_LIGHT) / (1.0 + MINECRAFT_AMBIENT_LIGHT);
    diffuse *= attenuate_no_cusp(length(offset), radius);

    if (occluded > 0.5) {
        vec3 normalWS = normalize((VeilCamera.IViewMat * vec4(normalVS, 0.0)).xyz);
        diffuse *= voxelshadowVisibility(pos + normalWS * 0.01, lightPos);
    }

    float reflectivity = 0.05;
    vec3 diffuseColor = diffuse * lightColor;
    fragColor = vec4(albedoColor.rgb * diffuseColor * (1.0 - reflectivity) + diffuseColor * reflectivity, 1.0);
}
#endif
