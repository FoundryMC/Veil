Veil provides a **JSON-based shader injection system** that allows modifying vanilla and pinwheel shaders without
replacing the entire file. Injections are written as plain GLSL with a `void tail()` or `void head()` marker function.

# Defining Injections

Injection definitions are located in `assets/modid/pinwheel/shader_injection/*.json`.
Each definition references one or more GLSL files that contain the actual shader code.

# JSON Format

```json5
{
  // The shader(s) to inject into. Can be a single string or array.
  "target": "minecraft:shaders/core/rendertype_solid.fsh",
  // The GLSL file(s) to inject. Required unless "replace" is used.
  "redirect": "modid:example.glsl",
  // Optional. Replace the target shader entirely with another Veil shader.
  // Mutually exclusive with redirect.
  "replace": "modid:custom_shader",
  // Optional. Lower values execute first. Default 1000.
  "priority": 1000,
  // Optional. Logs parsed body and globals for debugging.
  "debug": true
}
```

### Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `target` | `string` or `string[]` | **required** | Shader(s) to inject into |
| `redirect` | `string` or `string[]` | *see note* | GLSL file(s) providing the injection code |
| `replace` | `string` | — | Replace the target shader entirely with another Veil shader |
| `priority` | `int` | `1000` | Injection order (lower = sooner) |
| `debug` | `bool` | `false` | Log injection body and globals to console |

> `redirect` and `replace` are mutually exclusive. One of them must be present.

### Target Format

- **`redirect`** targets **must** include the shader extension (e.g. `.fsh`, `.vsh`):
  ```
  "target": "minecraft:shaders/core/rendertype_solid.fsh"
  ```
- **`replace`** targets do **not** include the extension:
  ```
  "target": "minecraft:shaders/core/rendertype_solid"
  ```
  The system strips shader extensions automatically for replace lookups.

### Multi-Target Example

```json
{
  "target": [
    "minecraft:shaders/core/rendertype_solid.vsh",
    "minecraft:shaders/core/rendertype_cutout.vsh"
  ],
  "redirect": "modid:scale.glsl"
}
```

### Replace

`replace` completely swaps the target shader with another Veil shader program defined in `pinwheel/shaders/program/`. Unlike `redirect`, which injects code into the existing shader, `replace` discards the original entirely and substitutes a different `ShaderProgram`.

- The target must be a vanilla Minecraft shader (e.g. `minecraft:shaders/core/rendertype_solid`) **without** file extension.
- The replacement must be a valid Veil shader program ID.
- Only vanilla Minecraft shaders can be replaced. To replace a Veil shader, use `redirect` with head/tail injections.

```json
{
  "target": "minecraft:shaders/core/rendertype_solid",
  "replace": "modid:custom_shader"
}
```

### Full Examples

#### head_example

**JSON** (`assets/modid/pinwheel/shader_injection/head_example.json`):
```json
{
  "target": "minecraft:shaders/core/rendertype_translucent.fsh",
  "redirect": "modid:head_example.glsl"
}
```

**GLSL** (`assets/modid/pinwheel/shader_injection/head_example.glsl`):
```glsl
// Affects translucent blocks: water, stained glass, ice, slime blocks, honey blocks.
// Desaturates to grayscale.
void tail() {
    float gray = dot(fragColor.rgb, vec3(0.299, 0.587, 0.114));
    fragColor.rgb = vec3(gray);
}
```

#### tail_example

**JSON** (`assets/modid/pinwheel/shader_injection/tail_example.json`):
```json
{
  "target": "minecraft:shaders/core/rendertype_cutout_mipped.fsh",
  "redirect": "modid:tail_example.glsl"
}
```

**GLSL** (`assets/modid/pinwheel/shader_injection/tail_example.glsl`):
```glsl
// Affects mipmapped cutout blocks: leaves, powered rails, decorated pots.
// Inverts the red and green channels for a psychedelic look.
void tail() {
    fragColor.r = 1.0 - fragColor.r;
    fragColor.g = 1.0 - fragColor.g;
}
```

#### multi_target

**JSON** (`assets/modid/pinwheel/shader_injection/multi_target.json`):
```json
{
  "target": [
    "minecraft:shaders/core/rendertype_entity_solid.fsh",
    "minecraft:shaders/core/rendertype_entity_cutout.fsh",
    "minecraft:shaders/core/rendertype_entity_translucent.fsh"
  ],
  "redirect": "modid:multi_target.glsl"
}
```

**GLSL** (`assets/modid/pinwheel/shader_injection/multi_target.glsl`):
```glsl
// Affects all entity rendering: mobs, players, item entities, armor stands.
// Applies a red overlay to everything.
void tail() {
    fragColor.rgb = mix(fragColor.rgb, vec3(1.0, 0.0, 0.0), 0.3);
}
```

#### replace_example

Requires a `color` shader program in `assets/modid/pinwheel/shaders/program/`.

**Program JSON** (`assets/modid/pinwheel/shaders/program/color.json`):
```json
{
  "vertex": "modid:color",
  "fragment": "modid:color"
}
```

**Vertex** (`assets/modid/pinwheel/shaders/program/color.vsh`):
```glsl
layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec2 UV1;
layout(location = 3) in vec2 UV2;
layout(location = 4) in vec4 Color;
layout(location = 5) in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;

out vec2 texCoord0;
out vec4 vertexColor;

void main() {
    vec3 pos = Position + ChunkOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    texCoord0 = UV0;
    vertexColor = Color;
}
```

**Fragment** (`assets/modid/pinwheel/shaders/program/color.fsh`):
```glsl
uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    fragColor = vertexColor;
}
```

**Injection JSON** (`assets/modid/pinwheel/shader_injection/replace_example.json`):
```json
{
  "target": "minecraft:shaders/core/rendertype_solid",
  "replace": "modid:color"
}
```

# GLSL Format

Write standard GLSL. Use **`void tail()`** to inject at the **end** of the target function (usually `main`), or
**`void head()`** to inject at the **start**.

```glsl
void tail() {
    gl_Position = gl_Position * 2.0;
}
```

> **Warning:** If neither `void head()` nor `void tail()` is present, the injection is silently skipped and a warning is logged. At least one marker function is required.

### Globals

Code placed **outside** the marker function is treated as globals and injected at the top of the shader. This is
useful for uniforms, helper functions, or shared variables.

```glsl
vec4 tintColor(vec4 color) {
    return color * vec4(1.0, 0.5, 0.5, 1.0);
}

void tail() {
    fragColor = tintColor(fragColor);
}
```

### Version

If your injection requires a specific GLSL version, add `#version` at the top of the file. It will be auto-detected
and applied if the target shader's version is lower.

```glsl
#version 330

void tail() {
    gl_Position = gl_Position * 2.0;
}
```

If omitted, the version is handled automatically.

Standard GLSL syntax is fully supported, including comments, control flow, multi-line statements, and nested blocks.

See [Shader.md](Shader.md) for #include

# Migration from the Old Format

| Old (`.txt`) | New (JSON + GLSL) |
|--------------|-------------------|
| `#version 330` | Optional. Auto-detected from GLSL file if present. |
| `#priority 1000` | `"priority": 1000` in JSON |
| `#include modid:path` | `#include "path.glsl"` in GLSL file |
| `#replace modid:path` | `"replace": "modid:path"` in JSON |
| `[FUNCTION main(0) HEAD]` | `void head() { }` in GLSL file |
| `[FUNCTION main(0) TAIL]` | `void tail() { }` in GLSL file |
| `[OUTPUT]`, `[UNIFORM]`, `[GET_ATTRIBUTE]` | Write directly as globals outside `tail()`/`head()` |
| `#name` placeholders | Use variables directly |
| `assets/.../shader_modifiers/*.txt` | `assets/.../shader_injection/*.json` |
