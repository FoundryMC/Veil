#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;
uniform float time;
uniform vec2 InSize;
uniform vec3 CameraPos;
uniform samplerBuffer Data;

out vec4 fragColor;

float fetch(int index){
    return texelFetch(Data, index).r;
}

float dist(vec3 p1, vec3 p2){
    return sqrt(dot(p1, p2));
}


void main()
{
    vec3 center = vec3(fetch(0), fetch(1), fetch(2));
    // check if CameraPos is within a sphere of radius 10.0
    float dista = dist(center, CameraPos);
    fragColor = texture(DiffuseSampler, texCoord) * vec4(dista, dista, dista, 1.0);
    //fragColor = vec4(lumav, lumav, lumav, 1.0);
}