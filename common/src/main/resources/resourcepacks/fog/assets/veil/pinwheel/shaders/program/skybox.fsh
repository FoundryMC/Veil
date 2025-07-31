#include veil:space_helper

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform samplerCube Skybox;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec3 dir = viewDirFromUv(texCoord);
    float depthSample = texture(DiffuseDepthSampler, texCoord).r;
    if (depthSample >= 1) {
        fragColor = texture(Skybox, vec3(-dir.x, dir.y, dir.z));
    } else {
        fragColor = texture(DiffuseSampler0, texCoord);
    }
}
