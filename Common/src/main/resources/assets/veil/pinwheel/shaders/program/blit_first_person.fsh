#include veil:blend

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D FirstPersonSampler;
uniform sampler2D FirstPersonDepthSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 diffuse = texture(DiffuseSampler0, texCoord);
    vec4 firstPerson = texture(FirstPersonSampler, texCoord);
    fragColor.rgb = blend(diffuse, firstPerson);
    fragColor.a = diffuse.a + firstPerson.a;
    gl_FragDepth = min(texture(DiffuseDepthSampler, texCoord).r, texture(FirstPersonDepthSampler, texCoord).r);
}
