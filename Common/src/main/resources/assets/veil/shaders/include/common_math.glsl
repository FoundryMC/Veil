// https://github.com/MightyPirates/Scannable/blob/1.18-forge/src/main/resources/assets/scannable/shaders/core/scan_effect.fsh
vec3 getWorldPos(float depth, vec2 texCoord, mat4 invProjMat, mat4 invViewMat, vec3 cameraPos) {
    float z = depth * 2.0 - 1.0;
    vec4 clipSpacePosition = vec4(texCoord * 2.0 - 1.0, z, 1.0);
    vec4 viewSpacePosition = invProjMat * clipSpacePosition;
    viewSpacePosition /= viewSpacePosition.w;
    vec4 worldSpacePosition = invViewMat * viewSpacePosition;

    return cameraPos + worldSpacePosition.xyz;
}

float linearizeDepth(float depth, float near, float far) {
    near *= 2.;
    far *= 2.;
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

float getWorldDepth(float depth, float near, float far, vec2 texCoord, float fov, float aspectRatio) {
    return linearizeDepth(depth, near, far);
}

vec2 texCoord2NDC(vec2 tc) {
    return tc * 2.0 - 1.0;
}

vec2 NDC2TexCoord(vec2 ndc) {
    return (ndc + 1.0) / 2.0;
}

vec3 rayFromNDC(vec2 ndc, vec3 lookVector, vec3 leftVector, vec3 upVector, float nearPlaneDistance, float fov, float aspectRatio) {
    vec3 planeMid = lookVector * nearPlaneDistance;
    float fovMulY = nearPlaneDistance * tan(fov / 2.);
    float fovMulX = fovMulY * aspectRatio;
    return normalize(planeMid + (leftVector * -ndc.x * fovMulX) + (upVector * ndc.y * fovMulY));
}

// https://www.youtube.com/watch?v=HFPlKQGChpE
struct RSIResult { bool didIntersect; float tNear; vec3 insNear; bool hasFar; float tFar; vec3 insFar; };
// s: sphere center    r: sphere radius
RSIResult raySphereIntersection(vec3 ray, vec3 s, float r) {
    float osd = length(s); // distance between ray origin and sphere center

    float t = dot(s, ray);
    vec3 p = ray*t;

    float y = length(s-p);
    if (osd >= r) { // ray origin is outside of the sphere
        if (y>r) return RSIResult(false, 0., vec3(0.), false, 0., vec3(0.));

        float x = sqrt(r*r - y*y);
        float t1 = t-x;
        float t2 = t+x;
        vec3 ins1 = ray * t1;
        vec3 ins2 = ray * t2;
        if (t1 < t2) {
            if (dot(ins2, ray) < 0.) return RSIResult(false, 0., vec3(0.), false, 0., vec3(0.));
            return RSIResult(true, t1, ins1, true, t2, ins2);
        } else {
            if (dot(ins1, ray) < 0.) return RSIResult(false, 0., vec3(0.), false, 0., vec3(0.));
            return RSIResult(true, t2, ins2, true, t1, ins1);
        }
    } else {
        float x1 = sqrt(osd*osd - y*y);// ray origin to P
        float x2 = sqrt(r*r - y*y);// P to intersection
        float t;
        if (dot(ray, s) >= 0.) {
            t = x1+x2;
        } else {
            t = x2-x1;
        }
        return RSIResult(true, t, ray*t, false, 0., vec3(0.));
    }
}