uniform sampler2D LightSampler;
uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;
uniform sampler2D TransparentSampler;
uniform sampler2D TransparentDepthSampler;

uniform vec4 ColorModulator;

in vec2 texCoord;

out vec4 fragColor;

vec3 blend(vec3 dst, vec4 src) {
    return (dst * (1.0 - src.a)) + src.rgb;
}

void main() {
    vec4 main = texture(MainSampler, texCoord);
    vec4 light = texture(LightSampler, texCoord);
    vec4 transparent = texture(TransparentSampler, texCoord);
    fragColor = vec4(main.rgb, 1.0) * ColorModulator;
    fragColor.rgb = mix(fragColor.rgb, light.rgb, light.a);
    fragColor.rgb = blend(fragColor.rgb, transparent);
    gl_FragDepth = min(texture(MainDepthSampler, texCoord).r, texture(TransparentDepthSampler, texCoord).r);
}
