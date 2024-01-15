#include veil:camera
#include veil:material

in vec2 texCoord;

uniform sampler2D NormalSampler;
uniform usampler2D MaterialSampler;
uniform sampler2D LightMapSampler;

uniform float LightShading0;
uniform float LightShading1;
uniform float LightShading2;
uniform float LightShading3;
uniform float LightShading4;
uniform float LightShading5;

out vec4 fragColor;

float getVanillaBrightness(vec3 Normal) {
    vec3 worldNormal = (VeilCamera.IViewMat * vec4(Normal, 0.0)).xyz;

    float darkFromD = pow(clamp(-worldNormal.y, 0.0, 1.0), 3) * LightShading0;
    float darkFromU = pow(clamp(worldNormal.y, 0.0, 1.0), 3) * LightShading1;
    float darkFromN = pow(clamp(-worldNormal.z, 0.0, 1.0), 2) * LightShading2;
    float darkFromS = pow(clamp(worldNormal.z, 0.0, 1.0), 2) * LightShading3;
    float darkFromW = pow(clamp(-worldNormal.x, 0.0, 1.0), 2) * LightShading4;
    float darkFromE = pow(clamp(worldNormal.x, 0.0, 1.0), 2) * LightShading5;

    return darkFromD + darkFromU + darkFromN + darkFromS + darkFromW + darkFromE;
}

void main() {
    vec3 normalVS = texture(NormalSampler, texCoord).xyz;
    vec4 lightmap = texture(LightMapSampler, texCoord);
    uint material = texture(MaterialSampler, texCoord).r;
    fragColor = lightmap;
    if (isBlock(material)) {
        fragColor.rgb *= vec3(getVanillaBrightness(normalVS));
    }
}
