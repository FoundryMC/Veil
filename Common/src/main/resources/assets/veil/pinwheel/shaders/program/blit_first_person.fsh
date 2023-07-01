uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D FirstPersonSampler;
uniform sampler2D FirstPersonDepthSampler;

in vec2 texCoord;

out vec4 fragColor;

vec3 blend(vec3 dst, vec4 src) {
    return (dst * (1.0 - src.a)) + src.rgb;
}

void main() {
    vec4 firstPerson = texture(FirstPersonSampler, texCoord);
    fragColor = vec4(texture(DiffuseSampler0, texCoord).rgb, 1.0);
    if (firstPerson.a == 0) {
        gl_FragDepth = texture(DiffuseDepthSampler, texCoord).r;
    } else {
        fragColor.rgb = blend(fragColor.rgb, firstPerson);
        gl_FragDepth = texture(FirstPersonDepthSampler, texCoord).r;
    }
}
