Framebuffers can be used globally with the [`FramebufferManager`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/render/framebuffer/FramebufferManager.java), and also with [post-processing](PostProcessing).
Globally scoped framebuffers are located in the `framebuffers` folder in the resource pack. These can be used in post-processing to move data between stages.

# Format

There is a short and long form of the buffer format. The short version only has 1 color buffer and an optional depth buffer. The long version allows full customization of all color buffers and an optional depth buffer. **There must be at least 1 color buffer!** It is not valid to have a depth-only framebuffer.

### Long-Form

```json5
{
  // Optional
  // This is a MoLang expression
  "width": "q.screen_width",
  // Optional
  // This is a MoLang expression
  "height": "q.screen_height",
  // Optional
  // Whether the buffer should be automatically cleared at the start of every frame
  "autoClear": true,
  // Required
  "color_buffers": [
    {
      // Optional
      // "texture" or "render_buffer"
      "type": "texture",
      // Optional
      // The OpenGL format to use
      "format": "RGBA8",
      // Optional
      // The number of mipmaps for textures and samples for render buffers. This must be at least 0
      "levels": 0,
      // Optional
      // Whether to use linear or nearest filtering
      "linear": false,
      // Optional
      // The custom name to use when uploading this as a sampler to shaders
      "name": "AwesomeColorBuffer"
    }
  ],
  // See #Depth
  "depth": ...,
}
```

### Short-Form

```json5
{
  // Optional
  // This is a MoLang expression
  "width": "q.screen_width",
  // Optional
  // This is a MoLang expression
  "height": "q.screen_height",
  // Optional
  // Whether the buffer should be automatically cleared at the start of every frame
  "autoClear": true,
  // Optional
  // "texture" or "render_buffer"
  "type": "texture",
  // Optional
  // The OpenGL format to use
  "format": "RGBA8",
  // Optional
  // The number of mipmaps for textures and samples for render buffers. This must be at least 0
  "levels": 0,
  // Optional
  // Whether to use linear or nearest filtering
  "linear": false,
  // Optional
  // The custom name to use when uploading this as a sampler to shaders
  "name": "AwesomeColorBuffer",
  // See #Depth
  "depth": ...,
}
```

### Depth

Depth can either be a boolean to add a depth buffer, or a full attachment definition.

```json5
{
  "depth": true
}
```

**OR**

```json5
{
  "depth": {
    // Optional
    // "texture" or "render_buffer"
    "type": "texture",
    // Optional
    // The OpenGL format to use
    "format": "DEPTH_COMPONENT",
    // Optional
    // The number of mipmaps for textures and samples for render buffers. This must be at least 0
    "levels": 0,
    // Optional
    // Whether to use linear or nearest filtering
    "linear": false,
    // Optional
    // The custom name to use when uploading this as a sampler to shaders
    "name": "AwesomeDepthBuffer"
  }
}
```

# Notes

When using an integer or floating point format, make _**SURE**_ the data type is set appropriately. It may or may not
work on some systems, but most drivers will refuse to draw into a buffer if the data type is mismatched!

All parameters in framebuffer definitions are optional to make it very easy to create new quick buffers. For example,
the following is a valid framebuffer definition:

```json5
{
  "depth": true
}
```

This creates a framebuffer the size of the window. It has a single color texture attachment using `RGBA8` and `0` mipmap
levels. It also has a depth texture buffer using `DEPTH_COMPONENT`.

Setting `name` in a texture attachment will only add an alias binding. This means:

```json5
{
  "depth": true,
  "color_buffers": [
    {
      "name": "AlbedoSampler",
      "format": "RGBA8"
    },
    {
      "name": "NormalSampler",
      "format": "RGB16F"
    },
    {
      "name": "MaterialSampler",
      "format": "R16F"
    },
    {
      "name": "EmissiveSampler",
      "format": "RGBA8"
    },
    {
      "name": "VanillaLightSampler",
      "format": "RG8"
    }
  ]
}
```

can reference `AlbedoSampler` in the shader. For example:

```glsl
// Both of these reference the same texture
uniform sampler2D DiffuseSampler0;
uniform sampler2D AlbedoSampler;
```

`linear` and `name` only work for `texture` attachment types. Any other types ignore this parameter (since you can’t
sample render buffers).
