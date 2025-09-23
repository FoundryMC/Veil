uniform sampler2D Sampler0;
uniform sampler2D Noise;

uniform vec4 ColorModulator;
uniform float GameTime;
uniform float Speed;
uniform vec4 ColorMultiplier;

in vec2 texCoord0;
in float vertexDistance;
in vec4 vertexColor;
in vec3 vertexNormal;

out vec4 fragColor;

void main() {
    float time = GameTime * 1200.0 * 3.14159 * Speed * 15.0;
    float uvTime = GameTime * 120.0 * Speed * 5.0;
    float cosT = cos(time - 10.0 * texCoord0.y);
    float sinT = sin(time - 10.0 * texCoord0.y);
    //    fragColor = vec4(1+cosT, 1.0 + sinT*cosT, 1.0+sinT, vertexColor.a * 0.1 / (1.0 - vertexColor.a)) * ColorMultiplier;
    fragColor = vec4(1.0, 1.0, 1.0, vertexColor.a * 0.5 / (1.0 - vertexColor.a)) * ColorMultiplier;
    fragColor *= texture(Noise, vec2(texCoord0.x * ColorMultiplier.a, uvTime)).a;
    fragColor = vec4(1.0);
}

