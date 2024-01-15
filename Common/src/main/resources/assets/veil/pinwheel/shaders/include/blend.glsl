#define TRANSLUCENT_TRANSPARENCY 0
#define ADDITIVE_TRANSPARENCY 1
#define LIGHTNING_TRANSPARENCY 2
#define GLINT_TRANSPARENCY 3
#define CRUMBLING_TRANSPARENCY 4
#define NO_TRANSPARENCY 5

vec3 blend(vec4 dst, vec4 src) {
    return src.rgb + (dst.rgb * (1 - src.a));
}

vec3 blendAdditive(vec4 dst, vec4 src) {
    return src.rgb + dst.rgb;
}

vec3 blendLightning(vec4 dst, vec4 src) {
    return src.rgb * src.a + dst.rgb;
}

vec3 blendGlint(vec4 dst, vec4 src) {
    return src.rgb * src.rgb + dst.rgb;
}

vec3 blendCrumbling(vec4 dst, vec4 src) {
    return src.rgb * dst.rgb + dst.rgb * src.rgb;
}

vec3 blend(uint material, vec4 dst, vec4 src) {
    if (material != NO_TRANSPARENCY) {
        if (material == TRANSLUCENT_TRANSPARENCY) {
            return blend(dst, src);
        }
        if (material == ADDITIVE_TRANSPARENCY) {
            return blendAdditive(dst, src);
        }
        if (material == LIGHTNING_TRANSPARENCY) {
            return blendLightning(dst, src);
        }
        if (material == GLINT_TRANSPARENCY) {
            return blendGlint(dst, src);
        }
        if (material == CRUMBLING_TRANSPARENCY) {
            return blendCrumbling(dst, src);
        }
    }
    return src.a == 0 ? dst.rgb : src.rgb;
}