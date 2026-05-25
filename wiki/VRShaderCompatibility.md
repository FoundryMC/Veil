# VR Shader Compatibility

Veil has an optional Vivecraft-aware rendering path for post-processing and lights. The goal is simple: regular Veil
shaders should keep working in desktop Minecraft, and should also render correctly when Vivecraft is installed and VR is
active.

Most shaders do not need separate left-eye and right-eye versions. They do need to avoid a few assumptions that are safe
on a monitor but wrong in a headset.

In VR, treat each eye as a separate render:

- its own camera
- its own projection and view matrices
- its own framebuffer size
- its own color, depth, normal, light, velocity, and history buffers

If a shader follows those rules, Veil can usually make it work automatically.

## Compatibility Checklist

For most post shaders, VR compatibility is a review of assumptions rather than a rewrite. Start with the existing shader
and check the items below.

1. Keep the normal shader path.

   Avoid separate VR-only shader variants unless the effect genuinely needs different logic. Declare the VR uniforms only
   when the shader needs them:

   ```glsl
   uniform int VeilIsVR;
   uniform int VeilEyeIndex;
   uniform vec2 VeilViewportSize;
   ```

   In desktop rendering, `VeilIsVR` is `0`, so the shader can keep its existing behavior.

2. Replace hardcoded screen-size math.

   If the shader uses the Minecraft window size, a cached framebuffer size, or constants like `854x480`, replace that
   with the active viewport size:

   ```glsl
   vec2 pixelSize = 1.0 / VeilViewportSize;
   ```

   Vivecraft renders each eye to its own target. That target is commonly a different size from the game window.

3. Use `VeilCamera` for world-space effects.

   Any shader that reconstructs world position, places light, samples depth in world space, or compares against camera
   position should use Veil's camera buffer:

   ```glsl
   #veil:buffer veil:camera VeilCamera
   ```

   Use `VeilCamera.ViewMat`, `VeilCamera.ProjMat`, `VeilCamera.IViewMat`, `VeilCamera.IProjMat`, and
   `VeilCamera.CameraPosition`. Veil updates these for the active eye, so the shader receives the correct left-eye or
   right-eye camera automatically.

4. Split anything that remembers the previous frame.

   Temporal effects, reprojection, velocity, accumulation, TAA-style history, and previous-frame bloom or exposure data
   must not be shared between eyes. If the shader or mod code chooses the history texture manually, branch on
   `VeilEyeIndex`.

5. Reduce uncomfortable fullscreen effects.

   Motion blur, camera shake, and strong distortion should be scaled in VR:

   ```glsl
   float blurStrength = baseBlurStrength * VeilMotionBlurScale;
   float shakeStrength = baseShakeStrength * VeilCameraShakeScale;
   float distortionStrength = baseDistortionStrength * VeilDistortionScale;
   ```

6. Avoid writing directly to the active eye target unless the pass owns the final output.

   Prefer temporary post buffers and `veil:post`. This lets Veil resolve the result back into the correct Vivecraft eye
   target without breaking GUI, alpha, depth, or compositor state.

If a shader only samples the current color texture with `texCoord` and writes a color, it may already be compatible.
Problems usually appear when the shader uses screen size, depth reconstruction, temporal history, custom framebuffers, or
strong screen-space distortion.

## What Veil Handles

When Vivecraft is active, Veil runs post-processing against the current eye target instead of the normal desktop target.
The non-VR rendering path is left unchanged.

During a left or right eye pass, Veil also keeps its internal buffers separated per eye. This includes post buffers,
temporary pipeline buffers, dynamic buffers used by lights, bloom buffers, and temporal history resources that Veil owns.

For shader code, Veil exposes a small set of VR-specific uniforms when they are declared:

```glsl
uniform int VeilIsVR;
uniform int VeilEyeIndex; // 0 = left, 1 = right, -1 = non-eye / non-VR
uniform vec2 VeilViewportSize;
```

Camera data still comes from the normal Veil camera buffer:

```glsl
#veil:buffer veil:camera VeilCamera
```

Use the existing camera fields:

- `VeilCamera.ViewMat`
- `VeilCamera.ProjMat`
- `VeilCamera.IViewMat`
- `VeilCamera.IProjMat`
- `VeilCamera.CameraPosition`

In VR, Veil updates that buffer for the active eye. Shader authors should not need separate `LeftViewMatrix` or
`RightViewMatrix` uniforms.

Veil also uploads comfort scale uniforms for effects that are risky in VR:

```glsl
uniform float VeilMotionBlurScale;
uniform float VeilCameraShakeScale;
uniform float VeilDistortionScale;
```

These are `1.0` in normal desktop rendering and can be reduced in VR by config.

## Writing VR-Safe Shaders

The most important rule is to use the active render size, not the desktop window size. In Vivecraft, the eye framebuffer
is often much larger than the Minecraft window.

```glsl
vec2 pixelSize = 1.0 / VeilViewportSize;
vec2 uv = gl_FragCoord.xy * pixelSize;
```

If your pass already receives a correct `texCoord` from Veil, keep using it. `VeilViewportSize` is most useful when a
shader does pixel math, blur kernels, screen-space offsets, depth reconstruction, or manual UV generation.

For world-space reconstruction, use the camera buffer for the current eye:

```glsl
#veil:buffer veil:camera VeilCamera

vec3 viewPosFromDepth(vec2 uv, float depth) {
    vec4 clipPos = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewPos = VeilCamera.IProjMat * clipPos;
    return viewPos.xyz / viewPos.w;
}

vec3 worldPosFromDepth(vec2 uv, float depth) {
    vec4 viewPos = vec4(viewPosFromDepth(uv, depth), 1.0);
    vec4 worldPos = VeilCamera.IViewMat * viewPos;
    return worldPos.xyz + VeilCamera.CameraPosition;
}
```

Do not reconstruct world position from a cached desktop camera, a window-sized projection, or a matrix saved before
Vivecraft starts rendering the eye. That is the usual cause of lights or world-space effects looking stuck to the screen.

## Effects To Scale In VR

Some fullscreen effects are comfortable on a monitor but unpleasant in a headset. Use Veil's scale uniforms instead of
hard-disabling the whole shader when possible:

```glsl
float blurStrength = baseBlurStrength * VeilMotionBlurScale;
float shakeStrength = baseShakeStrength * VeilCameraShakeScale;
float distortionStrength = baseDistortionStrength * VeilDistortionScale;
```

Recommended VR behavior:

- Motion blur: disabled, or extremely subtle.
- Camera shake: usually disabled.
- Strong distortion, refraction, fisheye, or warping: heavily reduced.
- Chromatic aberration: subtle and symmetric per eye.
- Vignette: subtle, unless it is intentionally being used as a comfort vignette.

## Framebuffers And History

Do not intentionally share these between left and right eyes:

- color buffers
- depth buffers
- normal buffers
- material mask buffers
- light color buffers
- velocity buffers
- temporal history buffers

Temporary framebuffers inside a post pipeline are the safest place for private intermediate work:

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

Veil isolates temporary pipeline framebuffers per eye. Data-driven global framebuffers are also handled per eye while
Vivecraft is actively rendering a stereo pass, but temporary pipeline buffers are clearer and easier to reason about.

If mod code creates its own framebuffer or history texture, use the same rule Veil uses internally: keep one resource
set per eye, and only resize it when the eye target size actually changes.

For manual temporal history selection, use `VeilEyeIndex`:

```glsl
vec4 history = VeilEyeIndex == 1
    ? texture(RightEyeHistorySampler, texCoord)
    : texture(LeftEyeHistorySampler, texCoord);
```

If an effect cannot keep separate left and right history, disable that effect in VR.

## Depth And Alpha

Be careful with clears and alpha writes during VR rendering. Vivecraft owns the eye targets and composites world, GUI,
and mirror output in its own path. Clearing or overwriting alpha on the wrong target can create hard cutoffs or missing
layers.

Good defaults:

- Clear temporary buffers, not the active Vivecraft eye target.
- Avoid writing alpha unless the pass owns final compositing.
- Prefer writing final post output to `veil:post`.
- Let Veil resolve `veil:post` back into the current eye target.

## Config

The VR path is quiet by default. The remaining options are focused on compatibility and quality:

```json
{
  "logVrSharedBufferWarnings": true,
  "useSodiumCompatibleVrFallback": true,
  "optimizeVrPerformance": true,
  "vrPostQuality": 0.6,
  "vrBloomQuality": 0.35,
  "vrLightQuality": 0.5
}
```

`logVrSharedBufferWarnings` warns once when a post pass references a framebuffer Veil cannot prove is isolated per eye.

`useSodiumCompatibleVrFallback` enables a safer path for VR setups where another renderer owns or changes framebuffer
state in ways Veil cannot rely on.

`optimizeVrPerformance` caps VR post, bloom, and light-buffer scales to more practical values. Turn it off only when you
want to test the exact values from `vrPostQuality`, `vrBloomQuality`, and `vrLightQuality`.

## Troubleshooting

| Symptom | Likely cause | Fix |
| --- | --- | --- |
| Effect appears only in part of the eye | The shader or framebuffer is using the desktop window size | Use `VeilViewportSize`; make sure custom buffers are allocated at the eye size |
| The world appears in a small lower-left box | A pass is bound to the desktop main target instead of the current eye target | Use Veil's post target flow and avoid caching `minecraft:main` assumptions |
| World-space lights or effects stick to the screen | Stale camera matrices or non-eye projection data | Use `VeilCamera` for the active eye |
| One eye looks different from the other | A buffer, history texture, or cached state is shared between eyes | Split the resource by `VeilEyeIndex` |
| Veil lights cut off part of the eye | Stencil, scissor, viewport, or depth state leaked into the light/fullscreen pass | Restore GL state around custom passes and ensure light buffers match `VeilViewportSize` |
| The image swims or feels uncomfortable | Motion blur, shake, or distortion is too strong for VR | Scale with the VR safety uniforms |

## Testing Checklist

Test these three cases before shipping a shader or effect:

- Vivecraft not installed.
- Vivecraft installed, but VR inactive.
- Vivecraft active in a headset.

For VR, check both eyes while moving your head and looking at world-space lights, held items, bright bloom sources, GUI
layers, and any effect that uses depth or history. A shader can run in VR without explicit VR code, but it is only truly
compatible if it uses the active eye size, the active eye camera, and separate per-eye history where needed.
