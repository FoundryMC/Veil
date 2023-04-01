#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D TranslucentSampler;
uniform sampler2D TranslucentDepthSampler;
uniform sampler2D ItemEntitySampler;
uniform sampler2D ItemEntityDepthSampler;
uniform sampler2D ParticlesSampler;
uniform sampler2D ParticlesDepthSampler;
uniform sampler2D WeatherSampler;
uniform sampler2D WeatherDepthSampler;
uniform sampler2D CloudsSampler;
uniform sampler2D CloudsDepthSampler;

in vec2 texCoord;

#define NUM_LAYERS 6

vec4 color_layers[NUM_LAYERS];
float depth_layers[NUM_LAYERS];
int active_layers = 0;

out vec4 fragColor;

void try_insert( vec4 color, float depth ) {
    if ( color.a == 0.0 ) {
        return;
    }

    color_layers[active_layers] = color;
    depth_layers[active_layers] = depth;

    int jj = active_layers++;
    int ii = jj - 1;
    while ( jj > 0 && depth_layers[jj] > depth_layers[ii] ) {
        float depthTemp = depth_layers[ii];
        depth_layers[ii] = depth_layers[jj];
        depth_layers[jj] = depthTemp;

        vec4 colorTemp = color_layers[ii];
        color_layers[ii] = color_layers[jj];
        color_layers[jj] = colorTemp;

        jj = ii--;
    }
}

vec3 blend( vec3 dst, vec4 src ) {
    return ( dst * ( 1.0 - src.a ) ) + src.rgb;
}

#define near 0.05
#define far  1000.0
float sampleDepth(vec2 uv) {
    float z = (texture(DiffuseDepthSampler, uv).r * 2.0 - 1.0);
    return pow((2.0 * near * far) / (far + near - z * (far - near)), 0.1);
}

void main() {
    color_layers[0] = vec4( texture( DiffuseSampler, texCoord ).rgb, 1.0 );
    depth_layers[0] = texture( DiffuseDepthSampler, texCoord ).r;
    active_layers = 1;

    try_insert( texture( TranslucentSampler, texCoord ), texture( TranslucentDepthSampler, texCoord ).r );
    try_insert( texture( ItemEntitySampler, texCoord ), texture( ItemEntityDepthSampler, texCoord ).r );
    try_insert( texture( ParticlesSampler, texCoord ), texture( ParticlesDepthSampler, texCoord ).r );
    //try_insert( texture( WeatherSampler, texCoord ), texture( WeatherDepthSampler, texCoord ).r );
    //try_insert( texture( CloudsSampler, texCoord ), texture( CloudsDepthSampler, texCoord ).r );

    vec3 texelAccum = color_layers[0].rgb;
    for ( int ii = 1; ii < active_layers; ++ii ) {
        texelAccum = blend( texelAccum, color_layers[ii] );
    }


    const float scale = 2.0;

    vec2 texelSize = 1.0 / textureSize(DiffuseDepthSampler, 0);

    float ds0 = sampleDepth(texCoord + vec2(+1.0, +1.0) * texelSize * scale * 0.5);
    float ds1 = sampleDepth(texCoord + vec2(-1.0, -1.0) * texelSize * scale * 0.5);
    float ds2 = sampleDepth(texCoord + vec2(+1.0, -1.0) * texelSize * scale * 0.5);
    float ds3 = sampleDepth(texCoord + vec2(-1.0, +1.0) * texelSize * scale * 0.5);

    float depthDifference0 = abs(ds0 - ds1);
    float depthDifference1 = abs(ds2 - ds3);

    float depthOutline = sqrt(depthDifference0 * depthDifference0 + depthDifference1 * depthDifference1);
    depthOutline = clamp(depthOutline > 0.001 ? ((depthOutline - 0.001) * 100.0) : 0.0, 0.0, 0.8);

    //fragColor = vec4(mix(texelAccum.rgb, vec3(0.0, 0.0, 0.0), depthOutline), 1.0);
    vec4 tex = texture(DiffuseSampler, texCoord);
    fragColor.rgb = tex.rgb * mat3(
    .393, .769, .189,
    .349, .686, .168,
    .272, .534, .131);
    fragColor.a = 1;

    //fragColor = vec4( texelAccum.rgb, 1.0 );
    //fragColor = vec4(normalOutline * 10.0);
}
