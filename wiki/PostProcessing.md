Post-pipelines are created through a custom JSON structure that links shader files together. This is similar to the
vanilla mc post-chain, but with a lot more features.

## Syntax

```json5
{
  // Required
  "stages": [
    ...
  ],
  // Optional
  "textures": {
    ...
  },
  // Optional
  "framebuffers": {
    ...
  },
  // Optional
  "renderStage": "VeilRenderLevelStageEvent.Stage enum values",
  // Optional
  "dynamicBuffers": [
    "albedo",
    "normal",
    "light_uv",
    "light_color",
    "debug"
  ],
  // Optional
  "priority": 1000,
  // Optional
  "replace": false
}
```

### Parameters

- `priority` defines how multiple pipelines named the same thing will be merged. A lower value indicates this will be
  inserted before others.

- `replace` Indicates this pipeline will overwrite all pipelines of the same name with a higher priority value.

Post-pipelines are designed to allow easy injection into any pipeline by adding a file to the pack with the exact same
location/id. The priority and replace options are used to make it easier to fully configure how pipelines behave.

## Stages

Stages are the backbone of the entire pipeline system.

All existing types are listed in [`PostPipelineStageRegistry`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/registry/PostPipelineStageRegistry.java#L26-L29), and shader stage types are the basic building blocks of
post processing.

### Framebuffer Reference

When a framebuffer is used as a parameter, it can be accessed via its name. The id can either be a defined type in
the `framebuffers` folder (`"minecraft:main"`), or a temporary buffer created in the post pipeline JSON. Temporary
buffers are accessed by the defined string without the prefix (`"name"`).

The `in` parameter for all stages defines an input framebuffer id. The
regular [Framebuffer Reference](Framebuffer#Reference) rules apply. This parameter is optional because it sets
the shader uniforms `DiffuseSampler0`-`DiffuseSampler#`, where `#` is the maximum color buffer ID of the framebuffer.
It also sets `DiffuseDepthSampler` if the framebuffer has a depth buffer.

Any buffers with a custom name defined will also have the shader uniform of the same name updated.

### Stage Types

#### Blit
Blit is the most basic type that simply draws a quad to the screen with the specified shader.

```json5
{
  "type": "veil:blit",
  // Required
  "shader": "modid:shaderid",
  // Optional
  // The input scene is stored in veil:post
  "in": "modid:framebufferid",
  // Required
  // The framebuffer to draw into. It is generally good practice to draw into veil:post on the last stage
  "out": "modid:framebufferid",
  // Optional
  // Whether to clear the out buffer before drawing
  "clear": true
}
```

#### Copy
Copy is similar to blit, but specifically copies buffers from one framebuffer to another. That means there is no support
for a shader in this stage.

```json5
{
  "type": "veil:copy",
  // Optional
  // The input scene is stored in veil:post
  "in": "modid:framebufferid",
  // Required
  // The framebuffer to draw into. It is generally good practice to draw into veil:post on the last stage
  "out": "modid:framebufferid",
  // Optional
  // Whether to copy color buffers
  "color": false,
  // Optional
  // Whether to copy depth buffers
  "depth": false,
  // Optional
  // Whether to use linear filtering if the out size does not equal the in size
  "linear": false
}
```

#### Mask
Mask is sets the color and depth write state for all later stages. By default, post-processing stages have color
write enabled and depth write disabled.

**_NOTE:_** When depth write is enabled, make sure to write depth values with `gl_FragDepth`. Otherwise, the depth
buffer will be filled with 0.5.

```json5
{
  "type": "veil:mask",
  // Optional
  // Whether to write into the red channel
  "red": true,
  // Optional
  // Whether to write into the green channel
  "green": true,
  // Optional
  // Whether to write into the blue channel
  "blue": true,
  // Optional
  // Whether to write into the alpha channel
  "alpha": true,
  // Optional
  // Whether to write into the depth buffer
  "depth": false
}
```

#### Depth Function
Depth function sets the depth write function for all later stages. See
the [OpenGL Documentation](https://docs.gl/gl4/glDepthFunc) for more details on depth functions.

```json5
{
  "type": "veil:depth_function",
  // The function to use. The initial value is ALWAYS
  "function": "ALWAYS"
}
```

# Textures

Textures are identical to [shaders](Shader#Textures). The benefit is allowing post-pipelines to define more global
textures that can be referenced in all child shaders.

# Framebuffers

All framebuffers created in this pipeline are considered temporary and only accessible from this pipeline. To access a
framebuffer between pipelines, use the `framebuffers` folder to define global framebuffers. Framebuffers defined here
have the same format as [global framebuffers](Framebuffer).

## Fog Example
`assets/example/pinwheel/post/example.json`
```json5
{  
  "stages": [  
    {  
      "type": "veil:blit",
      "shader": "example:test_shader", // My custom shader
      "in": "minecraft:main"
    }  
  ]  
}
```
`assets/example/pinwheel/shaders/program/test_shader.json`
```json5
{  
  "vertex": "veil:blit_screen", // This should almost always be veil:blit_screen
  "fragment": "example:test_shader"  
}
```
`assets/example/pinwheel/shaders/program/test_shader.fsh`
```glsl
#include veil:fog  
#include veil:space_helper  

// The first color attachment from `in`
uniform sampler2D DiffuseSampler0;  
// The depth attachment from `in`
uniform sampler2D DiffuseDepthSampler;  
  
const float FogStart = -10;  
const float FogEnd = 40;  
uniform vec4 FogColor;  
uniform int FogShape;  
  
in vec2 texCoord;  
  
out vec4 fragColor;  
  
void main() {  
	// Sample from the screen
    vec4 baseColor = texture(DiffuseSampler0, texCoord);
    // Sample from the depth texture
    float depthSample = texture(DiffuseDepthSampler, texCoord).r;  
    // Calculate the camera-relative position
    vec3 pos = screenToLocalSpace(texCoord, depthSample).xyz;  

	// For fog, find the distance from the player
    float vertexDistance = fog_distance(pos, FogShape);  
    // Output the mixed fog with the vanilla fog equation
    fragColor = linear_fog(baseColor, vertexDistance, FogStart, FogEnd, FogColor);  
}
```