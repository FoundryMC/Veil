#include veil:blend

uniform sampler2D DeferredSampler;
uniform sampler2D DeferredDepthSampler;
uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;
uniform sampler2D TransparentSampler;
uniform usampler2D TransparentMaterialSampler;
uniform sampler2D TransparentDepthSampler;

uniform vec4 ColorModulator;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 main = texture(MainSampler, texCoord);
    vec4 deferred = texture(DeferredSampler, texCoord);
    vec4 transparent = texture(TransparentSampler, texCoord);
    uint transparentBlend = texture(TransparentMaterialSampler, texCoord).g;
    fragColor = vec4(main.rgb, 1.0) * ColorModulator;
    fragColor.rgb = blend(fragColor, deferred);
    fragColor.rgb = blend(transparentBlend, fragColor, transparent);
    gl_FragDepth = min(texture(MainDepthSampler, texCoord).r, min(texture(DeferredDepthSampler, texCoord).r, texture(TransparentDepthSampler, texCoord).r));
}
