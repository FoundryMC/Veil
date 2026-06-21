Dynamic buffers are a custom system that adds some deferred rendering features to the game. It essentially allows mods to choose what extra information they want to use later in post-processing or in a deferred rendering pass.
#### Supported Buffers

| Type           | Texture Location                  | Description             |
| -------------- | --------------------------------- | ----------------------- |
| Albedo         | `veil:dynamic_buffer/albedo`      | Raw unlit texture color |
| Normal         | `veil:dynamic_buffer/normal`      | Screen-relative normals |
| Lightmap UV    | `veil:dynamic_buffer/light_uv`    | Lightmap UV coordinates |
| Lightmap Color | `veil:dynamic_buffer/light_color` | Lightmap color          |
| Debug          | `veil:dynamic_buffer/debug`       | Unused by Veil          |

#### Enabling Buffers

Dynamic buffers are enabled based on `ResourceLocation`s and what buffers each one uses. To enable a given set of dynamic buffers, simply call [`VeilRenderer#enableBuffers`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/render/VeilRenderer.java#L120). Similarly, [`VeilRenderer#disableBuffers`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/render/VeilRenderer.java#L136) is used to disable any active buffers. These functions must be called on the render thread. 

In-game, dynamic buffers can be viewed using the editor menu (default keybind F6) in the Framebuffers section. They can also be enabled using the (client-sided) command `/veilc buffers enable <type>` 

## Custom Shader Support
Veil supports outputting to dynamic buffers automatically by adding a small tag to the value in your shader source. All fields support being marked with `// #veil:BUFFER` to automatically output it to the corresponding buffer when enabled.

This works in both the vertex and fragment shaders. Veil will automatically pass the value from the vertex shader to the fragment shader when needed.

Veil also adds flags when certain dynamic buffers are enabled in the shader:

| Buffer         | Definition Name    |
| -------------- | ------------------ |
| Albedo         | `VEIL_ALBEDO`      |
| Normal         | `VEIL_NORMAL`      |
| Lightmap UV    | `VEIL_LIGHT_UV`    |
| Lightmap Color | `VEIL_LIGHT_COLOR` |
| Debug          | `VEIL_DEBUG`       |

#### Example
`particle.vsh`
```glsl
#include veil:fog  
  
layout(location = 0) in vec3 Position;  
layout(location = 1) in vec2 UV0;  
layout(location = 2) in vec4 Color;  
layout(location = 3) in ivec2 UV2;  
#ifdef VEIL_NORMAL  
layout(location = 4) in vec3 Normal;  
#endif  
  
uniform sampler2D Sampler2;  
  
uniform mat4 ModelViewMat;  
uniform mat4 ProjMat;  
#ifdef VEIL_NORMAL  
uniform mat3 NormalMat;  
#endif  
  
out float vertexDistance;  
out vec2 texCoord0;  
out vec4 vertexColor;  
out vec4 lightmapColor;  
  
void main() {  
    vec4 WorldPosition = ModelViewMat * vec4(Position, 1.0);  
    gl_Position = ProjMat * WorldPosition;  
    vertexDistance = length(WorldPosition.xyz);  
    texCoord0 = UV0;  
    #ifdef VEIL_LIGHT_UV
    // This comment here specifies what the shader should output to the light UV buffer
    // #veil:light_uv  
    vec2 texCoord2 = vec2(UV2 / 256.0);  
    #endif  
    vertexColor = Color;  
    lightmapColor = texelFetch(Sampler2, UV2 / 16, 0);  
    #ifdef VEIL_NORMAL  
    // #veil:normal  
    vec3 normal = NormalMat * Normal;  
    #endif  
}
```
