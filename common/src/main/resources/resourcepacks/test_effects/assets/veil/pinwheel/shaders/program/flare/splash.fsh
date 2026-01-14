uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec4 _Time;
uniform float Shift;
uniform float Time;
uniform vec3 Blue;

in vec2 texCoord0;
in vec4 vertexColor;
in vec3 vertexNormal;

out vec4 fragColor;

void main() {
    float edginess = (1.0 - abs((texCoord0.x + 2.0*Time) * 2.0 - (1.0 + 2.0*Time)) / 2.0) + texCoord0.y;
    fragColor = texture(Sampler0, vec2(texCoord0.x, (texCoord0.y * Shift) / 32 ));
    fragColor *= 1.0 - fragColor.a;
    fragColor *= min(texCoord0.y * 0.125 / (0.525 - 0.5*texCoord0.y), 2.0);
    fragColor.rgb *= Blue;
    fragColor *= edginess;
    if (fragColor.a < 0.01) discard;
}

