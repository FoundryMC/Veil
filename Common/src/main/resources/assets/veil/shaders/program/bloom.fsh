#version 150

uniform sampler2D DiffuseSampler;

float blurSize = 2.0 / 512.0;
float intensity = 0.20;
in vec2 texCoord;
in vec2 oneTexel;
uniform vec2 InSize;

out vec4 fragColor;

#moj_import <veil:luma>
void main()
{
    float lumav = luma(texture(DiffuseSampler, texCoord).rgb);
//    if(lumav < 0.75)
//    {
//        fragColor = texture(DiffuseSampler, texCoord);
//        return;
//    }
    //vec2 texel = 1.0 / textureSize(DiffuseSampler, 0);
    vec4 color = vec4(0.0);
    vec2 offset = vec2(1.3846153846) * oneTexel;
   // vec2 tcoord = texCoord.xy / InSize.xy;
    color += texture(DiffuseSampler, vec2(texCoord.x - 4.0*blurSize, texCoord.y)) * 0.05;
    color += texture(DiffuseSampler, vec2(texCoord.x - 3.0*blurSize, texCoord.y)) * 0.09;
    color += texture(DiffuseSampler, vec2(texCoord.x - 2.0*blurSize, texCoord.y)) * 0.12;
    color += texture(DiffuseSampler, vec2(texCoord.x - blurSize, texCoord.y)) * 0.15;
    color += texture(DiffuseSampler, vec2(texCoord.x, texCoord.y)) * 0.16;
    color += texture(DiffuseSampler, vec2(texCoord.x + blurSize, texCoord.y)) * 0.15;
    color += texture(DiffuseSampler, vec2(texCoord.x + 2.0*blurSize, texCoord.y)) * 0.12;
    color += texture(DiffuseSampler, vec2(texCoord.x + 3.0*blurSize, texCoord.y)) * 0.09;
    color += texture(DiffuseSampler, vec2(texCoord.x + 4.0*blurSize, texCoord.y)) * 0.05;

    color += texture(DiffuseSampler, vec2(texCoord.x, texCoord.y - 4.0*blurSize)) * 0.05;
    color += texture(DiffuseSampler, vec2(texCoord.x, texCoord.y - 3.0*blurSize)) * 0.09;
    color += texture(DiffuseSampler, vec2(texCoord.x, texCoord.y - 2.0*blurSize)) * 0.12;
    color += texture(DiffuseSampler, vec2(texCoord.x, texCoord.y - blurSize)) * 0.15;
    color += texture(DiffuseSampler, vec2(texCoord.x, texCoord.y)) * 0.16;
    color += texture(DiffuseSampler, vec2(texCoord.x, texCoord.y + blurSize)) * 0.15;
    color += texture(DiffuseSampler, vec2(texCoord.x, texCoord.y + 2.0*blurSize)) * 0.12;
    color += texture(DiffuseSampler, vec2(texCoord.x, texCoord.y + 3.0*blurSize)) * 0.09;
    color += texture(DiffuseSampler, vec2(texCoord.x, texCoord.y + 4.0*blurSize)) * 0.05;
    //intensity = intensity * lumav;
    fragColor = color * intensity + texture(DiffuseSampler, texCoord);
    //fragColor = vec4(lumav, lumav, lumav, 1.0);
}