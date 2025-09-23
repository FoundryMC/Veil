
layout (triangles) in;
layout (line_strip, max_vertices = 30) out;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in float vertexDistance[3];
in vec4 vertexColor[3];
in vec2 texCoord0[3];
in GS_INPUT {
//    vec3 worldPosition;
    vec3 normal;
    vec2 noiseTexCoord0;
    vec2 noiseTexCoord1;
} gs_in[3];

out vec4 fVertexColor;

void createVertex(vec4 pos, vec4 color) {
    gl_Position = ProjMat * ModelViewMat * pos;
    fVertexColor = color;
    EmitVertex();
}

void main() {
//    fVertexColor = vec4(1.0,0.0,0.0,1.0);
//    const float third = 1.0/3.0;
//    vec4 center = third * gl_in[0].gl_Position + third * gl_in[1].gl_Position + third * gl_in[2].gl_Position;
//    vec3 normal = third * gs_in[0].normal + third * gs_in[1].normal + third * gs_in[2].normal;
//    normal = normalize(normal);
//    createVertex(center, vec4(normal,1.0));
//    createVertex(center + vec4(normal, 0.0), vec4(normal,1.0));
//    EndPrimitive();
    for (int i = 0; i < 3; i++) {
        vec3 normal = normalize(gs_in[i].normal);
        createVertex(gl_in[i].gl_Position, vec4(normal, 1.0));
        createVertex(gl_in[i].gl_Position + vec4(normal, 0.0), vec4(normal, 1.0));
        EndPrimitive();
    }
}