vec3 blend(vec3 dst, vec4 src) {
    return (dst * (1.0 - src.a)) + src.rgb;
}