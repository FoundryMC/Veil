uniform sampler2D DiffuseDepthSampler;
uniform sampler2D AuxDepthSampler;

in vec2 texCoord;

void main() {
    float main = texture(DiffuseDepthSampler, texCoord).r;
    float aux = texture(AuxDepthSampler, texCoord).r;
    gl_FragDepth = min(main, aux);
}
