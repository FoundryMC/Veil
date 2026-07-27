uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D LightSampler;
uniform sampler2D LightInscatteringSampler;

uniform vec4 ColorModulator;
uniform vec2 ScreenSize;

in vec2 texCoord;

out vec4 fragColor;

#define BOXRADIUS 3

vec3 boxBlur(vec2 uv, vec2 size)
{
    int kernel_window_size = BOXRADIUS * 2 + 1;
    int samples = kernel_window_size * kernel_window_size;

    vec3 color = vec3(0);

    float wsum = 0.0;
    for (int ry = -BOXRADIUS; ry <= BOXRADIUS; ++ry)
    for (int rx = -BOXRADIUS; rx <= BOXRADIUS; ++rx)
    {
        float w = 1.0;
        wsum += w;
        color += texture(LightInscatteringSampler, uv + vec2(rx, ry) / size).rgb * w;
    }

    return color/wsum;
}

void main() {
    vec4 main = texture(DiffuseSampler0, texCoord);
    float mainDepth = texture(DiffuseDepthSampler, texCoord).r;
    vec3 light = texture(LightSampler, texCoord).rgb + boxBlur(texCoord, ScreenSize / 4.0).rgb;
    fragColor = vec4(main.rgb + light, main.a);
    gl_FragDepth = mainDepth;
}
