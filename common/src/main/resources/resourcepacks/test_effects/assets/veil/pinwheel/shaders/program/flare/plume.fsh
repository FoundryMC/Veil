uniform sampler2D Sampler0;
uniform sampler2D Noise;

uniform vec4 ColorModulator;
uniform vec4 _Time;
uniform float Speed;
uniform vec4 ColorMultiplier;

in vec2 texCoord0;
in vec4 vertexColor;
in vec3 vertexNormal;

in vec2 texCoord1;

out vec4 fragColor;

void main() {
    float uvTime = _Time.y * Speed;
    fragColor = vec4(texCoord0.y * 0.5 / (1.0 - texCoord0.y)) * ColorMultiplier;
    fragColor *= texture(Noise, vec2(texCoord1.x * 0.5 * ColorMultiplier.a, texCoord1.y * 0.041 + uvTime)).a;
}

