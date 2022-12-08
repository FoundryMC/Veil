#version 330

uniform sampler2D DiffuseSampler;
uniform sampler2D MainDepthSampler;

uniform samplerBuffer Data;
uniform int instanceCount;

uniform float time;
uniform float fov;
uniform float aspectRatio;
uniform vec3 cameraPos;
uniform vec3 lookVector;
uniform vec3 upVector;
uniform vec3 leftVector;
uniform float nearPlaneDistance;
uniform float farPlaneDistance;

in vec2 texCoord;

out vec4 fragColor;

#moj_import <fufo:noise>
#moj_import <fufo:common_math>

float fetch(int index) {
    return texelFetch(Data, index).r;
}

float calcPattern(vec3 worldPos, float intensity) {
    float it, fa;

    float pattern = 0.;

    it = magicEnergyEffect(floor((worldPos + vec3(100., 0., 0.))*16.)/128.);
    fa = intensity/it;
    fa = fa*fa;
    pattern += 1. / ((it*1.) / (fa*.01));

    it = magicEnergyEffect(floor((worldPos + vec3(0., 100., 0.))*16.)/256.);
    fa = intensity/it;
    fa = fa*fa;
    pattern += 1. / ((it*1.) / (fa*.01));

    it = magicEnergyEffect(floor((worldPos + vec3(0., 0., 100.))*16.)/512.);
    fa = intensity/it;
    fa = fa*fa;
    pattern += 1. / ((it*1.) / (fa*.01));

    return pattern;
}

vec3 energySphere(vec3 ray, float worldDepth, vec3 center, float radius, vec3 baseColor, float intensity) {
    float mul = 0.;

    RSIResult rsi = raySphereIntersection(ray, center - cameraPos, radius);

    if (rsi.didIntersect) {
        if (rsi.tNear < worldDepth) {
            float fadeNearCamera = min(rsi.tNear / 10., 1.);
            fadeNearCamera = fadeNearCamera*fadeNearCamera*fadeNearCamera;
            mul += calcPattern(rsi.insNear + cameraPos, intensity) * fadeNearCamera;
        }
        if (rsi.hasFar) {
            if (rsi.tFar < worldDepth) {
                mul += calcPattern(rsi.insFar  + cameraPos, intensity);
            }
        }
    }

    return baseColor * mul;
}

void main() {
    vec3 orgCol = texture(DiffuseSampler, texCoord).xyz; // the original color of this pixel

    vec2 ndc = texCoord2NDC(texCoord); // normalized device coordinate (-1 to 1)
    vec3 ray = rayFromNDC(ndc, lookVector, leftVector, upVector, nearPlaneDistance, fov, aspectRatio);
    float depth = texture(MainDepthSampler, texCoord).r;
    float worldDepth = getWorldDepth(depth, nearPlaneDistance, farPlaneDistance, texCoord, fov, aspectRatio);

    fragColor = vec4(orgCol, 1.0);

    for (int ins=0; ins<instanceCount; ins++) {
        int i = ins * 8;
        vec3 center = vec3(
        fetch(i),
        fetch(i+1),
        fetch(i+2)
        );
        vec3 baseColor = vec3(
        fetch(i+3),
        fetch(i+4),
        fetch(i+5)
        );
        float radius = fetch(i+6);
        float intensity = fetch(i+7);
        fragColor += vec4(energySphere(ray, worldDepth, center, radius, baseColor, intensity), 0.);
    }
}
