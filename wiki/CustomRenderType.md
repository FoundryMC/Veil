Data-driven render types are located in the `rendertypes` folder in the resource pack. Use [`VeilRenderType#get`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/render/rendertype/VeilRenderType.java#L211) to get a
data-driven render type by id. Veil automatically caches the render types so there is no issue with putting this in a
render loop.

Render types can be created using files in the resource pack instead of directly through code. This makes it easier for
users to create new render types and edit existing ones.

_**Note: `VeilRenderType#get` calls should go in the render loop so whenever the render type updates in the resource
pack it will also update the rendering**_

## Example

```json
{
  "format": "POSITION_COLOR_TEX_LIGHTMAP",
  "mode": "QUADS",
  "bufferSize": "TRANSIENT",
  "sort": false,
  "affectsCrumbling": true,
  "outline": false,
  "layers": [
    {
      "type": "minecraft:texture",
      "texture": "%1$s",
      "blur": true,
      "mipmap": false
    },
    {
      "type": "veil:shader",
      "name": "veil:test_shader"
    },
    {
      "type": "minecraft:depth_test",
      "mode": "always"
    },
    {
      "type": "minecraft:cull"
    },
    {
      "type": "minecraft:lightmap"
    },
    {
      "type": "minecraft:write_mask",
      "color": true,
      "depth": false
    }
  ]
}
```

`test_rendertype.json`

```java
import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.Veil;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class TestRenderer {

    private static final ResourceLocation RENDER_TYPE = ResourceLocation.fromNamespaceAndPath("examplemod", "test_rendertype");

    public static void render(MultiBufferSource bufferSource) {
        RenderType renderType = VeilRenderType.get(RENDER_TYPE, "test_texture.png");
        if (renderType == null) {
            // There was an error loading the render type
            return;
        }

        VertexConsumer builder = bufferSource.getBuffer(renderType);
        // do rendering code as usual
    }
}
```

`TestRenderer.java`

## Syntax

```json5
{
  // Required
  // The vertex format to use. See DefaultVertexFormats
  "format": "POSITION",
  // Required
  // The type of primitives to construct and render with
  "mode": "QUADS",
  // Required
  // The size of the buffer in bytes. Alternatively, you can use BIG, SMALL, and TRANSIENT
  "bufferSize": "TRANSIENT",
  // Optional
  // Whether to sort the vertex data before rendering it
  "sort": false,
  // Optional
  // Whether this render type should affect the breaking animation
  "affectsCrumbling": true,
  // Optional
  // Whether this render type should be present in entity outlines
  "outline": false,
  // Required
  // All render type shards to include. See below for the available layers
  "layers": [
    ...
  ]
}
```

**_Note: layers can also be an array of arrays to create a layered render type_**

## Layers

Layers are a single part of a render type definition. All string elements in render type fields support using java
[formatting codes](https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#syntax) to insert template values.
However since the same list is used for all fields, you should reference
elements directly (`%$1s` references the first element as a string for example).

### Texture

Assigns a texture to unit 0.

```json5
{
  "type": "minecraft:texture",
  // Required
  "texture": "formatting_string",
  // Optional
  "blur": false,
  // Optional
  "mipmap": false,
}
```

### Multi-Texture

A list of normal texture layers that assigns textures to texture units 0-11.

```json5
{
  "type": "minecraft:multi_texture",
  "textures": [
    {
      // Required
      "texture": "formatting_string",
      // Optional
      "blur": false,
      // Optional
      "mipmap": false,
    },
    {
      // Required
      "texture": "formatting_string",
      // Optional
      "blur": false,
      // Optional
      "mipmap": false,
    },
    // More layers can be specified up to 12 (The default minecraft render system max)
    ...
  ]
}
```

### Vanilla Shader

A specific vanilla minecraft shader to use. This is based on the provided shader file name, without the `minecraft`
namespace.

```json5
{
  "type": "minecraft:shader",
  // Required
  "name": "formatting_string",
}
```

### Veil Shader

A specific veil shader to use. This will work for any registered veil shader.

```json5
{
  "type": "veil:shader",
  // Required
  "name": "formatting_string",
}
```

### Transparency

The transparency blend mode to use.

Valid modes:

- none (default)
- additive
- lightning
- glint
- crumbling
- translucent

**_Note: Most blend modes require transparency sorting to be enabled to render properly_**

```json5
{
  "type": "minecraft:transparency",
  // Required
  "mode": "formatting_string",
}
```

### Depth-Test

The kind of depth testing to use. This determines _how_ fragments are chosen to render over others.

Valid modes:

- never
- less
- equal
- lequal (default)
- greater
- notequal
- gequal
- always

```json5
{
  "type": "minecraft:depth_test",
  // Required
  "mode": "formatting_string",
}
```

### Cull

The faces to cull if desired.

Valid faces:

- front
- back (default)
- front_and_back
- none

```json5
{
  "type": "minecraft:cull",
  // Required
  "face": "formatting_string",
}
```

### Lightmap

Enables/disables the lightmap texture. By default, this layer enables the lightmap.

```json5
{
  "type": "minecraft:lightmap",
  // Optional
  "enabled": true,
}
```

### Overlay

Enables/disables the overlay texture. By default, this layer enables the overlay.

```json5
{
  "type": "minecraft:overlay",
  // Optional
  "enabled": true,
}
```

### Bloom

Adds a bloom effect to your Render Type

```json5
{
  "layers": [
    [
      // Your Default layer
      {
        "type": "veil:shader",
        "name": "veil:test_shader"
      }
    ],
    [
      // Bloom
      {
        "type": "veil:shader",
        "name": "veil:test_bloom_shader"
      },
      {
        "type": "minecraft:output",
        "framebuffer": "veil:bloom"
      }
    ]
  ]
}
```

### Layering

Specifies the layering mode. This works by adding a small offset in view or polygon space to prevent Z-fighting in
meshes.

Valid modes:

- none
- polygon_offset (default)
- view_offset

```json5
{
  "type": "minecraft:layering",
  // Optional
  "mode": "polygon_offset",
}
```

### Output

The framebuffer to draw the result of this render into. The `framebuffer` field is the name of any transparency or
created veil framebuffer.

```json5
{
  "type": "minecraft:output",
  // Required
  "framebuffer": "formatting_string",
}
```

### Texturing

Sets the value of `TextureMatrix` in the shader to a scrolling UV coordinate for the vanilla enchantment glint effect.

```json5
{
  "type": "minecraft:texturing",
  // Required
  "scale": 1,
}
```

### Write Mask

Sets the flags to draw into the color and depth buffers.

```json5
{
  "type": "minecraft:write_mask",
  // Optional
  "color": true,
  // Optional
  "depth": true,
}
```

### Line

Sets the width of lines when rendering. If `width` is not specified, the window scale is used instead.

```json5
{
  "type": "minecraft:line",
  // Optional
  "width": 42,
}
```

### Color Logic

Enables OpenGL color logic with the specified operation. In Vanilla MC this is used for making the text selection in
GUIs an inverted color (or_reverse).

Valid operations:

- and
- and_inverted
- and_reverse
- clear
- copy
- copy_inverted
- equiv
- invert
- nand
- noop
- nor
- or
- or_inverted
- or_reverse
- set
- xor

```json5
{
  "type": "minecraft:color_logic",
  // Required
  "operation": "formatting_string",
}
```

### Patches

Sets the per-vertex patch size when using tessellation shaders.

```json5
{
  "type": "veil:patches",
  // Required
  "patchVertices": 4,
}
```

### Depth Clamp

Enables OpenGL [Depth Clamp](https://paroj.github.io/gltut/Positioning/Tut05%20Depth%20Clamping.html).

```json5
{
  "type": "veil:depth_clamp",
  // Optional
  "enabled": true,
}
```

### Multisample

Enables OpenGL [Multisample Drawing](https://learnopengl.com/Advanced-OpenGL/Anti-Aliasing).

This only works if drawing into a framebuffer with a multi-sampled render buffer attachment (samples > 0).

```json5
{
  "type": "veil:multisample",
  // Optional
  "enabled": true,
}
```

### Seamless Cubemap

Globally enables OpenGL [Seamless Cubemap Sampling](https://wikis.khronos.org/opengl/Cubemap_Texture#Seamless_cubemap).

This is not required when using a Veil Shader. Instead, this can be set as a [sampling parameter](Shader#texture-filters) per cubemap texture.

```json5
{
  "type": "veil:seamless_cubemap",
  // Optional
  "enabled": true,
}
```
