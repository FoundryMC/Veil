uniform sampler3D BlockGrid;
uniform vec3 GridOrigin;

#define VOXELSHADOW_GRID_SIZE 64
#define VOXELSHADOW_MAX_STEPS 128

float voxelshadowVisibility(vec3 fragPos, vec3 lightPos) {
    vec3 startG = fragPos - GridOrigin;
    vec3 endG = lightPos - GridOrigin;
    vec3 delta = endG - startG;
    float rayLen = length(delta);
    if (rayLen < 0.001) return 1.0;

    vec3 rDir = delta / rayLen;
    ivec3 cell = ivec3(floor(startG));
    ivec3 iStep = ivec3(sign(rDir));

    vec3 invAbs = 1.0 / max(abs(rDir), vec3(1e-5));
    vec3 tDelta = invAbs;

    vec3 cellF = vec3(cell);
    vec3 tMax;
    tMax.x = (rDir.x >= 0.0) ? (cellF.x + 1.0 - startG.x) * invAbs.x : (startG.x - cellF.x) * invAbs.x;
    tMax.y = (rDir.y >= 0.0) ? (cellF.y + 1.0 - startG.y) * invAbs.y : (startG.y - cellF.y) * invAbs.y;
    tMax.z = (rDir.z >= 0.0) ? (cellF.z + 1.0 - startG.z) * invAbs.z : (startG.z - cellF.z) * invAbs.z;

    for (int i = 0; i < VOXELSHADOW_MAX_STEPS; i++) {
        if (any(lessThan(cell, ivec3(0))) || any(greaterThanEqual(cell, ivec3(VOXELSHADOW_GRID_SIZE)))) break;
        if (i > 0 && texelFetch(BlockGrid, cell, 0).r > 0.5) return 0.0;

        if (tMax.x < tMax.y && tMax.x < tMax.z) {
            if (tMax.x >= rayLen) break;
            tMax.x += tDelta.x;
            cell.x += iStep.x;
        } else if (tMax.y < tMax.z) {
            if (tMax.y >= rayLen) break;
            tMax.y += tDelta.y;
            cell.y += iStep.y;
        } else {
            if (tMax.z >= rayLen) break;
            tMax.z += tDelta.z;
            cell.z += iStep.z;
        }
    }

    return 1.0;
}
