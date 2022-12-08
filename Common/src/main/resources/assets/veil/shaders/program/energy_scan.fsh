#version 330

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthMain;

uniform samplerBuffer Data;
uniform int instanceCount;

uniform float time;
uniform mat4 InvViewMat;
uniform mat4 InvProjMat;
uniform vec3 CameraPos;

in vec2 texCoord;

out vec4 fragColor;

#moj_import <veil:noise>
#moj_import <veil:common_math>

float fetch(int index) {
    return texelFetch(Data, index).r;
}

struct EnergyEffectResult { vec3 col; float mix; };
EnergyEffectResult energyEffect(float noiseValue, float distToCenter, float signedDistToRing, vec3 baseColor, float colorIntensity, float mixIntensity, float width, float fadeMaxDist, float fadeMinDist) {
    //    float distanceFade = (300. - distToCenter) / 150.;
    float distanceFade = (fadeMaxDist - distToCenter) / fadeMinDist;
    distanceFade = clamp(distanceFade, 0., 1.);
    distanceFade = distanceFade * distanceFade;

    //    float fa = .3/noiseValue;
    float fa = mixIntensity / noiseValue;
    fa = fa*fa;

    //    float energyFade = max(1. - (signedDistToRing / 100.), 0.);
    float energyFade = max(1. - (signedDistToRing / width), 0.);
    energyFade = energyFade * energyFade;
    energyFade *= distanceFade;

    if (signedDistToRing < 0.) {
        energyFade *= smoothstep(0., .5, signedDistToRing+.5);
    }

    vec3 col = baseColor / (noiseValue * sqrt(abs(signedDistToRing) * colorIntensity)) * distanceFade;
    float mix = clamp(fa/abs(signedDistToRing) * energyFade, 0., 1.);

    return EnergyEffectResult(col, mix);
}

vec3 magicEffect(float noiseValue, float distToCenter, float signedDistToRing, vec3 baseColor, float intensity, float width, float fadeMaxDist, float fadeMinDist) {
    //    float distanceFade = (300. - distToCenter) / 150.;
    float distanceFade = (fadeMaxDist - distToCenter) / fadeMinDist;
    distanceFade = clamp(distanceFade, 0., 1.);
    distanceFade = distanceFade * distanceFade;

    //    float magicFade = max(1. - (signedDistToRing / 200.), 0.);
    float magicFade = max(1. - (signedDistToRing / width), 0.);
    magicFade = magicFade * magicFade;
    magicFade *= distanceFade;

    //    return vec3(.5,.25,1.) * (noiseValue+.5) * magicFade;
    return baseColor * (noiseValue+intensity) * magicFade;
}

void main() {
    vec3 orgCol = texture(DiffuseSampler, texCoord).xyz; // the original color of this pixel

    float depth = texture(DepthMain, texCoord).r; // non-normalized depth
    vec3 worldPos = getWorldPos(depth, texCoord, InvProjMat, InvViewMat, CameraPos); // world coordinate of this pixel
    worldPos = floor((worldPos+.001)*16.)/16.; // voxelize world position

    float noiseValue = magicEnergyEffect(worldPos);

    vec3 magicColorAccumulator = vec3(0.);
    vec3 energyColorAccumulator = vec3(0.);
    float energyMixAccumulator = 0.;
    for (int ins=0; ins<instanceCount; ins++) {
        int i = ins * 19;

        vec3 center = vec3(fetch(i), fetch(i+1), fetch(i+2));
        float virtualRadius = fetch(i+3); // only for the calculation, actual max radius depends on the fadeMaxDist

        float distToCenter = distance(worldPos, center);
        if (distToCenter > virtualRadius +.5) continue;

        vec3 magicBaseColor = vec3(fetch(i+4), fetch(i+5), fetch(i+6));
        float magicIntensity = fetch(i+7);
        float magicWidth = fetch(i+8);
        float magicFadeMaxDist = fetch(i+9);
        float magicFadeMinDist = fetch(i+10);

        vec3 energyBaseColor = vec3(fetch(i+11), fetch(i+12), fetch(i+13));
        float energyColorIntensity = fetch(i+14);
        float energyMixIntensity = fetch(i+15);
        float energyWidth = fetch(i+16);
        float energyFadeMaxDist = fetch(i+17);
        float energyFadeMinDist = fetch(i+18);

        float signedDistToRing = virtualRadius - distToCenter;

        magicColorAccumulator += magicEffect(noiseValue, distToCenter, signedDistToRing, magicBaseColor, magicIntensity, magicWidth, magicFadeMaxDist, magicFadeMinDist);

        EnergyEffectResult energyResult = energyEffect(noiseValue, distToCenter, signedDistToRing, energyBaseColor, energyColorIntensity, energyMixIntensity, energyWidth, energyFadeMaxDist, energyFadeMinDist);
        energyColorAccumulator += energyResult.col;
        energyMixAccumulator += energyResult.mix;
    }

    fragColor = vec4(
    mix(orgCol, energyColorAccumulator, clamp(energyMixAccumulator, 0., 1.)),
    1.0
    );
    fragColor.xyz += magicColorAccumulator;
}