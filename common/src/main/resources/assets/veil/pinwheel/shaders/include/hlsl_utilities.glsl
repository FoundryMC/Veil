//Utilities for making Flare shaders or for porting HLSL shaders to GLSL.

#include veil:space_helper
#veil:buffer veil:camera VeilCamera

uniform mat4 ModelToWorld;
uniform mat4 IModelToWorld;

float saturate(float x) { return clamp(x, 0.0, 1.0); }
vec2 saturate(vec2 x) { return clamp(x, 0.0, 1.0); }
vec3 saturate(vec3 x) { return clamp(x, 0.0, 1.0); }
vec4 saturate(vec4 x) { return clamp(x, 0.0, 1.0); }
float lerp(float x, float y, float s) { return x + s * (y - x); }
vec2 lerp(vec2 x, vec2 y, float s) { return x + s * (y - x); }
vec2 lerp(vec2 x, vec2 y, vec2 s) { return x + s * (y - x); }
vec3 lerp(vec3 x, vec3 y, float s) { return x + s * (y - x); }
vec3 lerp(vec3 x, vec3 y, vec3 s) { return x + s * (y - x); }
vec4 lerp(vec4 x, vec4 y, float s) { return x + s * (y - x); }
vec4 lerp(vec4 x, vec4 y, vec4 s) { return x + s * (y - x); }
vec4 power(float x, vec4 y) { return pow(vec4(x), y); }
vec3 power(float x, vec3 y) { return pow(vec3(x), y); }
float mad(in float mvalue, in float avalue, in float bvalue) { return mvalue * avalue + bvalue; }
vec2 mad(in vec2 mvalue, in vec2 avalue, in vec2 bvalue) { return mvalue * avalue + bvalue; }
vec3 mad(in vec3 mvalue, in vec3 avalue, in vec3 bvalue) { return mvalue * avalue + bvalue; }
vec4 mad(in vec4 mvalue, in vec4 avalue, in vec4 bvalue) { return mvalue * avalue + bvalue; }
float depthSampleToWorldDepth(in float depthSample) {
    float f = depthSample * 2.0 - 1.0;
    return 2.0 * VeilCamera.NearPlane * VeilCamera.FarPlane / (VeilCamera.FarPlane + VeilCamera.NearPlane - f * (VeilCamera.FarPlane - VeilCamera.NearPlane));
}

float worldDepthToDepthSample(in float worldDepth) {
    return 0.5-0.5*(2*VeilCamera.NearPlane*VeilCamera.FarPlane/worldDepth-VeilCamera.FarPlane-VeilCamera.NearPlane)/(VeilCamera.FarPlane-VeilCamera.NearPlane);
}
vec4 computeScreenPos(vec4 clip) {
    vec4 screenPos = clip * 0.5;
    screenPos.xy = vec2(screenPos.x + screenPos.w, screenPos.y + screenPos.w);
    screenPos.zw = clip.zw;
    return screenPos;
}
vec4 worldToModelSpace(vec4 world) {
    return IModelToWorld * world;
}
vec4 modelToWorldSpace(vec4 model) {
    return ModelToWorld * model;
}
vec4 localToModelSpace(vec4 world) {
    return IModelToWorld * (world + vec4(VeilCamera.CameraPosition, 0.0));
}
vec4 modelToLocalSpace(vec4 model) {
    return (ModelToWorld * model) - vec4(VeilCamera.CameraPosition, 0.0);
}
vec3 worldToModelSpace(vec3 world) {
    return (IModelToWorld * vec4(world, 1.0)).xyz;
}
vec4 modelToWorldSpace(vec3 model) {
    return ModelToWorld * vec4(model, 1.0);
}
vec4 localToModelSpace(vec3 world) {
    return IModelToWorld * vec4(world + VeilCamera.CameraPosition, 1.0);
}
vec3 modelToLocalSpace(vec3 model) {
    return (ModelToWorld * vec4(model, 1.0)).xyz - VeilCamera.CameraPosition;
}
