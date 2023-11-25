#version 150

// base vertex shader for post processing

in vec4 Position;

out vec2 texCoord;

void main(){
    gl_Position = vec4(Position.xy - 1.0, 0.0, 1.0);

    texCoord = Position.xy * 0.5;
}