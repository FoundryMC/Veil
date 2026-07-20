uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D LightSampler;
uniform sampler2D LightInscatteringSampler;

uniform vec4 ColorModulator;
uniform vec2 ScreenSize;

in vec2 texCoord;

out vec4 fragColor;

// Taken from https://github.com/amilajack/gaussian-blur/tree/master under MIT license

vec4 blur9(sampler2D image, vec2 uv, vec2 resolution, vec2 direction) {
    vec4 color = vec4(0.0);
    vec2 off1 = vec2(1.3846153846) * direction;
    vec2 off2 = vec2(3.2307692308) * direction;
    color += texture2D(image, uv) * 0.2270270270;
    color += texture2D(image, uv + (off1 / resolution)) * 0.3162162162;
    color += texture2D(image, uv - (off1 / resolution)) * 0.3162162162;
    color += texture2D(image, uv + (off2 / resolution)) * 0.0702702703;
    color += texture2D(image, uv - (off2 / resolution)) * 0.0702702703;
    return color;
}

void main() {
    vec4 main = texture(DiffuseSampler0, texCoord);
    float mainDepth = texture(DiffuseDepthSampler, texCoord).r;
    vec3 light = texture(LightSampler, texCoord).rgb + mix(
            blur9(LightInscatteringSampler, texCoord, ScreenSize / 4.0, vec2(1, 0)), blur9(LightInscatteringSampler, texCoord, ScreenSize / 4.0, vec2(0, 1)), vec4(0.5)).rgb;
    fragColor = vec4(main.rgb + light, main.a);
    gl_FragDepth = mainDepth;
}
