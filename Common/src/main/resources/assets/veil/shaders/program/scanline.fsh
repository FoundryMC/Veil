#version 150

uniform sampler2D DiffuseSampler;

float blurSize = 2.0 / 512.0;
float intensity = 0.20;
in vec2 texCoord;
in vec2 oneTexel;
uniform float time;
uniform vec2 InSize;

out vec4 fragColor;

float density = 1.8;
float opacityScanline = .3;
float opacityNoise = .2;
float flickering = 0.01;

float random (vec2 st) {
    return fract(sin(dot(st.xy,
    vec2(12.9898,78.233)))*
    43758.5453123);
}

float blend(const in float x, const in float y) {
    return (x < 0.5) ? (2.0 * x * y) : (1.0 - 2.0 * (1.0 - x) * (1.0 - y));
}

vec3 blend(const in vec3 x, const in vec3 y, const in float opacity) {
    vec3 z = vec3(blend(x.r, y.r), blend(x.g, y.g), blend(x.b, y.b));
    return z * opacity + x * (1.0 - opacity);
}

void main()
{
    vec3 col = texture(DiffuseSampler, texCoord).rgb;
    float count = InSize.y * density;
    vec2 sl = vec2(sin(texCoord.y * count), cos(texCoord.y * count));
    vec3 scanlines = vec3(sl.x, sl.y, sl.x);

    col += col * scanlines * opacityScanline;
    col += col * vec3(random(texCoord*time)) * opacityNoise;
    col += col * sin(110.0*time)*flickering;

    fragColor = vec4(col, 1.0);
    //fragColor = vec4(lumav, lumav, lumav, 1.0);
}