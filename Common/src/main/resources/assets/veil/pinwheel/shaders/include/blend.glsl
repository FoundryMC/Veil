#define TRANSLUCENT_TRANSPARENCY 0.0
#define ADDITIVE_TRANSPARENCY 1.0
#define LIGHTNING_TRANSPARENCY 2.0
#define GLINT_TRANSPARENCY 3.0
#define CRUMBLING_TRANSPARENCY 4.0
#define NO_TRANSPARENCY 5.0

vec3 blend(vec4 dst, vec4 src) {
    return src.rgb + (dst.rgb * (1.0 - src.a));
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

vec3 blend(float material, vec4 dst, vec4 src) {
    if (material != NO_TRANSPARENCY) {
        if (abs(material - TRANSLUCENT_TRANSPARENCY) < 0.1) {
            return blend(dst, src);
        }
        if (abs(material - ADDITIVE_TRANSPARENCY) < 0.1) {
            return blendAdditive(dst, src);
        }
        if (abs(material - LIGHTNING_TRANSPARENCY) < 0.1) {
            return blendLightning(dst, src);
        }
        if (abs(material - GLINT_TRANSPARENCY) < 0.1) {
            return blendGlint(dst, src);
        }
        if (abs(material - CRUMBLING_TRANSPARENCY) < 0.1) {
            return blendCrumbling(dst, src);
        }
    }
    return src.a == 0.0 ? dst.rgb : src.rgb;
}