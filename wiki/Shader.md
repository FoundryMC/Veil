Veil adds a custom shader format that supports all OpenGl shader stages. It also has additional features that make
shaders more data-driven.

# Defining Shaders

Shader programs are located in `assets/modid/pinwheel/shaders/program`. These are used as actual shader programs that
can be used during runtime.

Shader includes are located in `assets/modid/pinwheel/shaders/include`. These are glsl source files, but they cannot be
used as programs. Instead, they can be referenced in shader programs to share code between multiple shader programs.

# Programs

Shader programs define what shader stages should be used, what definitions to have, and what textures to bind. Compute
shaders are supported, but must be called from Java code. See [glDispatchCompute](https://docs.gl/gl4/glDispatchCompute)
for more information.

### Syntax

```json5
{
  // Optional
  "vertex": "modid:shaderid",
  // Optional
  "tesselation_control": "modid:shaderid",
  // Optional
  "tesselation_evaluation": "modid:shaderid",
  // Optional
  "geometry": "modid:shaderid",
  // Optional
  "fragment": "modid:shaderid",
  // Optional
  "compute": "modid:shaderid",
  // Optional
  "definitions": [
    "foo",
    "bar",
    {
      "defaultValue": 4
    }
  ],
  // Optional
  "textures": {
    "LocationTexture": "veil:textures/gui/item_shadow.png",
    "AlternateLocationTexture": {
      "type": "location",
      "location": "minecraft:textures/atlas/particles.png"
    },
    "ExampleFramebuffer": {
      "type": "framebuffer",
      "name": "veil:deferred",
      // This is used to identify what color buffer to sample from
      "sampler": 4
    },
    "ExampleFramebufferColor": {
      "type": "framebuffer",
      "name": "veil:deferred"
    },
    "ExampleFramebufferDepth": {
      "type": "framebuffer",
      "name": "veil:deferred:depth",
      "filter": {
        ...
      }
    }
  },
  // Optional
  "blend": {
    // Optional
    "func": "ADD",
    // Optional
    "alphafunc": "ADD",
    // Optional
    "srcrgb": "ONE",
    // Optional
    "dstrgb": "ZERO",
    // Optional
    "srcalpha": "ONE",
    // Optional
    "dstalpha": "ZERO"
  },
  // Optional
  "required_features": [
    "BINDLESS_TEXTURE",
    ...
    "See ShaderFeature.java for all supported features"
  ],
}
```

Each shader stage has a different file extension to allow each stage to share the same name as the shader. The only
exception is include shaders which always use `.glsl` as the file extension.

| Shader Stage           | Extension |
|------------------------|-----------|
| Vertex                 | `.vsh`    |
| Tesselation Control    | `.tcsh`   |
| Tesselation Evaluation | `.tesh`   |
| Geometry               | `.gsh`    |
| Fragment               | `.fsh`    |
| Compute                | `.comp`   |

# Includes

Include shaders define extra shader code that can be imported into programs and other includes. Included shaders are
allowed to include code from other include shaders as long as there are no circular references.

### Example

```glsl
#include namespace:path

out vec4 fragColor;

void main() {
    fragColor = vec4(1, 0, 1, 1);
}
```

### Double Include Example

`assets/veil/pinwheel/shaders/include/foo.glsl`

```glsl
vec3 foo(vec4 color) {
    return color.rgb;
}
```

`assets/veil/pinwheel/shaders/include/bar.glsl`

```glsl
#include veil:foo

vec3 bar(vec4 color) {
    return foo(color) / 2.0;
}
```

# Uniforms

Unlike vanilla shaders, no extra work is required to add uniforms to shaders. They will automatically be detected by
name when the methods in the shader are called.

Custom post-shader uniforms can be added by uploading uniforms during the `VeilPostProcessingEvent.Pre` event.

# Built-in Uniforms

| Source    | Java Code                                                               | GLSL Code                                   | Notes                                                                                                                                                                           |
|-----------|-------------------------------------------------------------------------|---------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Minecraft | `RenderSystem#getShaderTexture`                                         | `uniform sampler2D Sampler#;`               | Vanilla Minecraft supports samplers 0-11. Generally, the color texture is bound to `Sampler0`, the overlay is bound to `Sampler1`, and the lightmap is bound to `Sampler2`.     |
| Minecraft | `RenderSystem#getModelViewStack`                                        | `uniform mat4 ModelViewMat;`                |                                                                                                                                                                                 |
| Minecraft | `RenderSystem#getProjectionMatrix`                                      | `uniform mat4 ProjMat;`                     |                                                                                                                                                                                 |
| Minecraft | `RenderSystem#getTextureMatrix`                                         | `uniform mat4 TextureMat;`                  |                                                                                                                                                                                 |
| Minecraft | (`Window#getWidth`, `Window#getHeight`)                                 | `uniform vec2 ScreenSize;`                  |                                                                                                                                                                                 |
| Minecraft | `RenderSystem#getShaderColor`                                           | `uniform vec4 ColorModulator;`              |                                                                                                                                                                                 |
| Minecraft | `VeilRenderSystem#getLight0Direction`                                   | `uniform vec3 Light0_Direction;`            |                                                                                                                                                                                 |
| Minecraft | `VeilRenderSystem#getLight1Direction`                                   | `uniform vec3 Light1_Direction;`            |                                                                                                                                                                                 |
| Minecraft | `RenderSystem#getShaderGlintAlpha`                                      | `uniform float GlintAlpha;`                 |                                                                                                                                                                                 |
| Minecraft | `RenderSystem#getShaderFogStart`                                        | `uniform float FogStart;`                   |                                                                                                                                                                                 |
| Minecraft | `RenderSystem#getShaderFogEnd`                                          | `uniform float FogEnd;`                     |                                                                                                                                                                                 |
| Minecraft | `RenderSystem#getShaderFogColor`                                        | `uniform vec4 FogColor;`                    |                                                                                                                                                                                 |
| Minecraft | `RenderSystem#getShaderFogShape`                                        | `uniform int FogShape;`                     |                                                                                                                                                                                 |
| Minecraft | `RenderSystem#getShaderLineWidth`                                       | `uniform float LineWidth;`                  | Only present when using `LINES` or `LINE_STRIP` render mode                                                                                                                     |
| Minecraft | `RenderSystem#getShaderGameTime`                                        | `uniform float GameTime;`                   | Vanilla Minecraft sets this uniform to a value between 0 and 1 depending on the level game time. For example, 0 means the tick time is 0 and 0.5 means the game time is 12,000. |
| Minecraft | Set Manually per renderer                                               | `uniform vec3 ChunkOffset;`                 | This uniform is used during chunk rendering to offset the chunks in the shader.                                                                                                 |
| Veil      | The current client time in seconds, looping every hour                  | `uniform float VeilRenderTime;`             |                                                                                                                                                                                 |
| Veil      | The normal transformation from the projection matrix. `Matrix4f#normal` | `uniform mat3 NormalMat;`                   |                                                                                                                                                                                 |
| Veil      | `ClientLevel#getShade`                                                  | `uniform float VeilBlockFaceBrightness[#];` | Only set if the player is currently in a level. This should also be accessed using `#import veil:light` in a Veil shader.                                                       |

# Uniform Blocks

Veil has an API for creating simple uniform blocks, see [`VeilShaderBufferRegistry#REGISTRY`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/registry/VeilShaderBufferRegistry.java#L21). The data layout can then be
constructed with [`VeilShaderBufferLayout`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/render/VeilShaderBufferLayout.java), see [`CameraMatrices#createLayout`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/render/CameraMatrices.java#L43-L55) for an example.

The registry key used for a shader buffer can then be used in any veil shader to import the GLSL code to access that
block.

Veil fully supports registering custom uniform blocks. See [`VeilShaderBufferLayout#Builder`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/render/VeilShaderBufferLayout.java#L98) for more details.

### Example

```glsl
#veil:buffer veil:camera
```

This will include all the fields from `CameraMatrices#createLayout` in the top level of the shader. In most cases you
should also include an _interface name_ to make the shader more robust.

```glsl
#veil:buffer veil:camera FooBar
```

You can name the interface anything you want, but for best practice it should be related to what the block contains.
When a shader block interface name is set, all fields are accessed via `InterfaceName.FieldName` in the GLSL code.

# Built-in Uniform Blocks

| Java Code                        | GLSL Code                                |
|----------------------------------|------------------------------------------|
| `VeilRenderer#getCameraMatrices` | `#veil:buffer veil:camera VeilCamera`    |
| `VeilRenderer#getGuiInfo`        | `#veil:buffer veil:gui_info VeilGuiInfo` |

### Example

```java
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class RenderClass {

    private static final ResourceLocation CUSTOM_SHADER = ResourceLocation.fromNamespaceAndPath(Veil.MODID, "test_shader");

    public static void render(PoseStack stack, MultiBufferSource source, float partialTicks) {
        ShaderProgram shader = VeilRenderSystem.setShader(CUSTOM_SHADER);
        if (shader == null) {
            return;
        }

        ShaderUniformAccess customValue = shader.getUniform("CustomValue");
        // Always check if the uniform for nullability
        // because it will be null if it doesn't exist
        if (customValue != null) {
            customValue.setFloat(32.2F);
        }
        ShaderUniformAccess customProjection = shader.getUniform("CustomProjection");
        if (customProjection != null) {
            Matrix4f projection = new Matrix4f().ortho(0, 10, 10, 0, 0.3F, 100.0F, false);
            customProjection.setMatrix(projection);
        }

        shader.bind();
        // rendering code here
        ShaderProgram.unbind();
    }
}
```

### Post-Processing Example

```java
import foundry.veil.Veil;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import foundry.veil.platform.VeilEventPlatform;
import net.minecraft.resources.ResourceLocation;

public class MainModClass {

    private static final ResourceLocation CUSTOM_POST_PIPELINE = ResourceLocation.fromNamespaceAndPath(Veil.MODID, "test_pipeline");

    public MainModClass() {
        // This works for pipeline-specific uniforms
        VeilEventPlatform.INSTANCE.preVeilPostProcessing((pipelineName, pipeline, context) -> {
            if (CUSTOM_POST_PIPELINE.equals(pipelineName)) {
                ShaderUniformAccess secret = pipeline.getUniform("Secret");
                if (secret != null) {
                    secret.setInt(42);
                }
            }
        });
    }
}
```

# Definitions

Definitions allow shaders to use flags specified in Java code. Values can be defined by
calling `VeilRenderSystem.renderer().getDefinitions().define()`. Any shader that depends on a value will be
automatically recompiled when the value updates or is removed. Each definition is inserted as a `#define name value` in
shader code at the top of the file.

If a default value is specified, then that value will be used if no definition is specified in Java code.

## Global Definitions

Global definitions are similar to regular definitions, but they are added to all shaders and do not schedule a shader
recompile when changed. They should be set to constants defined in Java code.

### Syntax

```json5
{
  "definitions": [
    "foo",
    "bar",
    {
      "defaultValue": 4
    }
  ]
}
```

### Example

`assets/veil/pinwheel/shaders/program/example.json`

```json5
{
  "vertex": "veil:example",
  "fragment": "veil:example",
  "definitions": [
    "example_definition"
  ]
}
```

`Foo.java`

```java
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.shader.ShaderPreDefinitions;
import net.minecraft.resources.ResourceLocation;

public class Foo {

    private static final ResourceLocation SHADER_ID = ResourceLocation.fromNamespaceAndPath("veil", "example");

    // Some event fired before rendering
    public static void onPreRender() {
        VeilRenderer renderer = VeilRenderSystem.renderer();
        ShaderPreDefinitions definitions = renderer.getShaderDefinitions();

        // This adds #define EXAMPLE_DEFINITION to all shaders that depend on it
        definitions.set("example_definition");
    }
}
```

# Textures

All shader programs can define an arbitrary number of textures. There are currently two supported texture sources:

- Locations
- Framebuffers

The name used as the key in the json is bound to the uniform name in the shader files.

## Locations

This will use the name of any registered texture in `TextureManager` or load a texture from file if it does not exist.
This works exactly like binding any other texture in Minecraft:

```java
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.inventory.InventoryMenu;

public class Foo {

    public void render() {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
    }
}
```

## Framebuffers

This uses textures defined in framebuffers as texture sources. Since framebuffers can have multiple attachments, there
is an optional parameter defining what sampler to use. If the specified sampler doesn't exist or is not a texture
attachment, then no texture is bound.

Using `:depth` at the end of the name will use the depth attachment. If the framebuffer doesn't have a depth attachment,
then no texture is bound.

See the [framebuffer documentation](Framebuffer) for more information.

### Syntax

```json5
{
  "textures": {
    "LocationTexture": "veil:textures/gui/item_shadow.png",
    "AlternateLocationTexture": {
      "type": "location",
      "location": "minecraft:textures/atlas/particles.png"
    },
    "CubemapTexture": {
      "type": "location",
      "location": "veil:textures/somecubemap.png"
    },
    "ExampleFramebuffer": {
      "type": "framebuffer",
      "name": "veil:deferred",
      "sampler": 4
    },
    "ExampleFramebufferColor": {
      "type": "framebuffer",
      "name": "veil:deferred"
    },
    "ExampleFramebufferDepth": {
      "type": "framebuffer",
      "name": "veil:deferred:depth"
    }
  }
}
```

### Example

`assets/veil/pinwheel/shaders/program/example.json`

```json
{
  "vertex": "example",
  "fragment": "example",
  "textures": {
    "CustomTexture": "veil:textures/gui/item_shadow.png"
  }
}
```

`assets/veil/pinwheel/shaders/program/example.fsh`

```glsl
uniform sampler2D CustomTexture;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    fragColor = texture(CustomTexture, texCoord);
}
```

## Texture Filters

All textures can have their sampling parameters specified on a per-definition basis. If specified, this overrides any
parameters specified by the OpenGL texture object.

### Syntax

```json5
{
  "textures": {
    "TextureName": {
      "type": "location",
      "location": "minecraft:textures/atlas/particles.png",
      "filter": {
        "blur": false,
        // Whether the texture should use linear or nearest filtering
        "mipmap": false,
        // Whether the texture should use mipmaps
        "anisotropy": 1.0,
        // The maximum allowed level of "anisotropy". Any value > 1 enables anisotropic filtering (https://en.wikipedia.org/wiki/Anisotropic_filtering)
        "compareFunction": "never|always|less|lequal|equal|not_equal|gequal|greater",
        // Optional parameter. Indicates the type of depth comparison to make if a depth texture. Mostly used for shadow-mapping to allow correct interpolation when using blur (https://registry.khronos.org/OpenGL-Refpages/gl4/html/glTexParameter.xhtml)
        "wrapX|wrapY|wrapZ": "repeat|clamp_to_edge|clamp_to_border|mirrored_repeat|mirror_clamp_to_edge",
        // Default is repeat. Indicates how the texture should be sampled if the texture coordinates fall outside the range of 0 to 1 (for X/Y/Z S/T/R respectively)
        "borderColor": "0xFF000000",
        // Custom color when using clamp_to_border,
        "borderType": "float|int|uint",
        // Must be set when using an integer texture
        "seamless": false
        // Specifies cubemap textures to be sampled as seamless textures
      }
    }
  }
}
```

# Shader Blending

Normally, blending must be specified in java code and set per render type. However, Veil provides access to override the
currently set blend mode in the shader. This is mostly useful for post-processing, but it works in all scenarios.

See [`glBlendEquation`](https://registry.khronos.org/OpenGL-Refpages/gl4/html/glBlendEquation.xhtml) and
[`glBlendFuncSeparate`](https://registry.khronos.org/OpenGL-Refpages/gl4/html/glBlendFuncSeparate.xhtml) for more
information on changing the blend func.

### Syntax

```json5
{
  "blend": {
    // Optional
    "func": "ADD|SUBTRACT|REVERSE_SUBTRACT|MIN|MAX",
    // Optional
    "alphafunc": "ADD|SUBTRACT|REVERSE_SUBTRACT|MIN|MAX",
    // Optional
    "srcrgb": "CONSTANT_ALPHA|CONSTANT_COLOR|DST_ALPHA|DST_COLOR|ONE|ONE_MINUS_CONSTANT_ALPHA|ONE_MINUS_CONSTANT_COLOR|ONE_MINUS_DST_ALPHA|ONE_MINUS_DST_COLOR|ONE_MINUS_SRC_ALPHA|ONE_MINUS_SRC_COLOR|SRC_ALPHA|SRC_ALPHA_SATURATE|SRC_COLOR|ZERO",
    // Optional
    "dstrgb": "CONSTANT_ALPHA|CONSTANT_COLOR|DST_ALPHA|DST_COLOR|ONE|ONE_MINUS_CONSTANT_ALPHA|ONE_MINUS_CONSTANT_COLOR|ONE_MINUS_DST_ALPHA|ONE_MINUS_DST_COLOR|ONE_MINUS_SRC_ALPHA|ONE_MINUS_SRC_COLOR|SRC_ALPHA|SRC_COLOR|ZERO",
    // Optional
    "srcalpha": "CONSTANT_ALPHA|CONSTANT_COLOR|DST_ALPHA|DST_COLOR|ONE|ONE_MINUS_CONSTANT_ALPHA|ONE_MINUS_CONSTANT_COLOR|ONE_MINUS_DST_ALPHA|ONE_MINUS_DST_COLOR|ONE_MINUS_SRC_ALPHA|ONE_MINUS_SRC_COLOR|SRC_ALPHA|SRC_ALPHA_SATURATE|SRC_COLOR|ZERO",
    // Optional
    "dstalpha": "CONSTANT_ALPHA|CONSTANT_COLOR|DST_ALPHA|DST_COLOR|ONE|ONE_MINUS_CONSTANT_ALPHA|ONE_MINUS_CONSTANT_COLOR|ONE_MINUS_DST_ALPHA|ONE_MINUS_DST_COLOR|ONE_MINUS_SRC_ALPHA|ONE_MINUS_SRC_COLOR|SRC_ALPHA|SRC_COLOR|ZERO"
  }
}
```

# Shader Feature

Veil supports optional shader features through extensions. Instead of forcing the shader to attempt compilation and
fail, Veil requires all required features to be present before trying to load and compile the shader.

Requiring a feature will also enable all required GLSL extensions to use the feature.

### Syntax

```json5
{
  "required_features": [
    "A list of enum constants found in ShaderFeature.java"
  ]
}
```

### Example

`assets/veil/pinwheel/shaders/program/example.json`

```json5
{
  "vertex": "veil:example",
  "fragment": "veil:example",
  "required_features": [
    "BINDLESS_TEXTURE"
  ]
}
```

`assets/veil/pinwheel/shaders/program/example.vsh`

```glsl
layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord0 = UV0;
}
```

`assets/veil/pinwheel/shaders/program/example.fsh`

```glsl
in vec2 texCoord0;

layout(std140) uniform CustomTextures {
    sampler2D textures[128];
};

uniform uint TextureIndex;

out vec4 Color;

void main() {
    Color = texture(textures[TextureIndex], texCoord0);
}
```

Because bindless texture is a required extension, the shader will only load and compile if the GPU supports it.