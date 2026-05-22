# VR Shader Compatibility

Veil can run post-processing through a Vivecraft-aware path when Vivecraft is installed and VR is active. Most shaders do
not need separate left-eye and right-eye versions, but VR has a few rules that normal fullscreen shaders can get away
with ignoring.

The short version: treat every eye as its own camera, its own framebuffer chain, and its own temporal history.

## What Veil Provides

When Vivecraft is active and the current pass is a stereo eye pass, Veil automatically uploads these uniforms to post
shaders if they exist:

```glsl
uniform int VeilIsVR;
uniform int VeilEyeIndex; // 0 = left, 1 = right, -1 = non-eye / non-VR

uniform mat4 VeilViewMatrix;
uniform mat4 VeilProjectionMatrix;
uniform mat4 VeilInverseViewMatrix;
uniform mat4 VeilInverseProjectionMatrix;

uniform vec3 VeilCameraPosition;
uniform vec2 VeilViewportSize;
```

Veil also uploads safety scale uniforms:

```glsl
uniform float VeilMotionBlurScale;
uniform float VeilCameraShakeScale;
uniform float VeilDistortionScale;
```

Use these when an effect is uncomfortable or unsafe in VR.

## Basic VR-Safe Pattern

Prefer the viewport uniform instead of assuming the window size:

```glsl
vec2 pixelSize = 1.0 / VeilViewportSize;
vec2 uv = gl_FragCoord.xy * pixelSize;
```

For depth reconstruction, use the per-eye matrices:

```glsl
vec3 viewPosFromDepth(vec2 uv, float depth) {
    vec4 clipPos = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewPos = VeilInverseProjectionMatrix * clipPos;
    return viewPos.xyz / viewPos.w;
}

vec3 worldPosFromDepth(vec2 uv, float depth) {
    vec4 viewPos = vec4(viewPosFromDepth(uv, depth), 1.0);
    vec4 worldPos = VeilInverseViewMatrix * viewPos;
    return worldPos.xyz + VeilCameraPosition;
}
```

Do not reconstruct world position from the main window resolution or a cached non-VR camera. In VR, each eye has its own
projection, view matrix, camera position, and framebuffer size.

## Effects To Adjust In VR

Fullscreen effects that feel fine on a monitor can be uncomfortable in a headset. Scale them down automatically:

```glsl
float blurStrength = baseBlurStrength * VeilMotionBlurScale;
float shakeStrength = baseShakeStrength * VeilCameraShakeScale;
float distortionStrength = baseDistortionStrength * VeilDistortionScale;
```

Recommended behavior:

- Motion blur: disable in VR or keep extremely subtle.
- Camera shake: disable in VR.
- Strong warps/refraction/fisheye: reduce heavily in VR.
- Chromatic aberration: keep subtle and symmetric per eye.
- Vignette: keep subtle; avoid hard masks unless they are intentional comfort vignettes.

## Framebuffer Rules

In VR, never intentionally share these between eyes:

- color buffers
- depth buffers
- normal buffers
- material mask buffers
- light color buffers
- velocity buffers
- temporal history buffers

Use temporary framebuffers inside your post pipeline when possible:

```json5
{
  "framebuffers": {
    "blur_a": { "format": "RGBA16F", "depth": false },
    "blur_b": { "format": "RGBA16F", "depth": false }
  },
  "stages": [
    { "type": "veil:blit", "shader": "example:blur_x", "in": "minecraft:main", "out": "blur_a" },
    { "type": "veil:blit", "shader": "example:blur_y", "in": "blur_a", "out": "veil:post" }
  ]
}
```

Veil isolates temporary pipeline framebuffers per eye automatically. Data-driven global framebuffers are also copied per
eye when Vivecraft is actively rendering a left/right eye pass, but temporary framebuffers are still the cleaner choice
for private intermediate passes.

Veil's built-in dynamic buffers, including albedo and normal buffers used by Veil lights, are also isolated and resized
per eye. Do not cache the desktop window size for dynamic-buffer sampling; in a headset the eye target can be much larger
than the game window.

When drawing during a Vivecraft eye pass, use the active eye render target as the source of truth. The Minecraft window
target can be a mirror/desktop-sized target and is not necessarily the framebuffer currently being submitted to the
headset.

Veil keeps per-eye post, dynamic, bloom, and temporary composite framebuffers cached until the active eye size changes.
If you create custom buffers in mod code, follow the same rule: resize only when the eye target size actually changes,
and keep left/right history resources separate.

## Debug And Performance Config

The VR path is quiet by default. For diagnosis, enable these options in `config/veil-client.json`:

```json
{
  "debugVREyeBuffers": true,
  "debugVRPerformance": true,
  "vrDebugLogInterval": 120,
  "logVrSharedBufferWarnings": true,
  "saveVRFrameTimeHistory": false,
  "vrFrameTimeHistoryInterval": 1,
  "vrFrameTimeHistoryFile": "logs/veil-vr-frame-times.csv",
  "useSodiumCompatibleVrFallback": true,
  "optimizeVrPerformance": true,
  "vrPostQuality": 0.6,
  "vrBloomQuality": 0.35,
  "vrLightQuality": 0.5
}
```

`debugVRPerformance` periodically logs per-eye post time, framebuffer resize/creation counts, blit/copy counts, and
Sodium-compatible fallback usage. It also prints rolling frame time as last/average/min/max milliseconds and estimated
FPS. Keep it off during normal gameplay.

`saveVRFrameTimeHistory` writes frame time samples to `vrFrameTimeHistoryFile`. Use `vrFrameTimeHistoryInterval: 1` for
every VR frame, or a higher value to sample less often. The CSV columns are `epochMillis,sample,frameMs,fps`.

`optimizeVrPerformance` caps the effective VR post, bloom, and light-buffer scales to performance-friendly values. Turn
it off only when you want to test the exact quality values from `vrPostQuality`, `vrBloomQuality`, and `vrLightQuality`.

## Temporal Effects

Temporal effects need one history per eye. Do not keep one global "previous frame" texture and sample it from both eyes.

Use `VeilEyeIndex` if your shader or mod code chooses a history texture manually:

```glsl
vec4 history = VeilEyeIndex == 1
    ? texture(RightEyeHistorySampler, texCoord)
    : texture(LeftEyeHistorySampler, texCoord);
```

Also reject or reset history when the viewport size changes:

```glsl
bool vr = VeilIsVR != 0;
vec2 pixelSize = 1.0 / VeilViewportSize;
```

If the effect cannot keep per-eye history, disable it in VR.

## Depth And Alpha Clears

Be careful when writing to `minecraft:main` or copying depth/alpha around. Vivecraft uses its own eye targets and
compositing path, so a normal-looking clear can break GUI/world composition or create hard cutoffs.

Good habits:

- Clear temporary buffers, not the main Vivecraft target.
- Avoid writing alpha unless the effect really owns the final compositing semantics.
- Prefer writing the final color to `veil:post`.
- Let Veil resolve `veil:post` back into the current eye target.

## Common Symptoms

**Effect only appears on part of the screen**

The shader is probably using the desktop window size instead of the VR eye framebuffer size. Use `VeilViewportSize`.
If the world appears in a small lower-left box, a custom framebuffer or dynamic buffer is likely still allocated at the
desktop window size, or bound to the desktop main target, instead of the current eye target.

**World-space effect is stuck to the screen**

The shader is probably reconstructing position with stale or non-eye matrices. Use `VeilViewMatrix`,
`VeilProjectionMatrix`, and their inverse uniforms.

**Veil lights cut off half of an eye**

This usually means an eye-mask stencil or scissor state leaked into a fullscreen/light pass. Veil's VR compatibility
path disables that state while Veil post-processing and light volumes render, then restores it afterward. Custom render
code should do the same if it draws fullscreen or screen-space lighting during Vivecraft eye passes. Also check that any
custom albedo, normal, or depth buffers used by the light pass are per-eye and match `VeilViewportSize`.

**Left and right eyes look different in a bad way**

Something is being shared between eyes. Check temporal history, custom global framebuffers, dynamic buffers, and cached
viewport state.

**Strong discomfort or swimming**

Reduce distortion, camera shake, motion blur, and screen-space offsets in VR.

## Quick Checklist

- Use `VeilViewportSize` for pixel size and UV math.
- Use `VeilEyeIndex` for any manual per-eye resources.
- Use `VeilIsVR` to branch only when needed.
- Use Veil's per-eye matrices for depth/world reconstruction.
- Keep temporal history separate per eye.
- Avoid sampling or writing custom shared buffers across both eyes.
- Scale motion blur, shake, and distortion with the VR safety uniforms.
- Avoid clearing alpha on the main target.
- Test with Vivecraft inactive, Vivecraft installed but VR inactive, and Vivecraft active in a headset.
