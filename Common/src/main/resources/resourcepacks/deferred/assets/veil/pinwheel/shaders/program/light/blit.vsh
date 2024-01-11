layout (location = 0) in vec3 Position;

out vec2 screenUv;

void main() {
    gl_Position = vec4(Position, 1.0);
    screenUv = Position.xy * 0.5 + 0.5;
}
