#include veil:fog
#include veil:space_helper

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;

const float FogStart = -10;
const float FogEnd = 100;
uniform vec4 FogColor;
uniform int FogShape;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 baseColor = texture(DiffuseSampler0, texCoord);
    vec3 viewPos = screenToLocalSpace(texCoord, texture(DiffuseDepthSampler, texCoord).r).xyz;

    float vertexDistance = fog_distance(viewPos, FogShape);
    fragColor = linear_fog(baseColor, vertexDistance, FogStart, FogEnd, FogColor);
}
