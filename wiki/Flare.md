Flare is a data driven effects system based on [waterfall](https://github.com/post-kerbin-mining-corporation/Waterfall),
it allows for both simple and complex effects to be created and rendered effortlessly.

❗ These paths go off of `assets/modid/flare`.

### Shells

To simply put - shells are models stripped of their texture, though they may still contain texture coordinates.

Shells, just like models, can be exported directly from [Blockbench](https://www.blockbench.net/).

Unlike models, shells are placed in `flare/shells/`,
and they natively support models larger than 3 blocks and rotations at all angles **(for a single axis!)**.

### Properties

Properties are variables that can hold many different types of values,
some hold shader uniforms, some hold model data, and some may hold both.

```json5
{
  //Required
  //This name is used when applying a uniform to a shader
  "name": "Color",
  //Required
  //This defines what type of data this property should hold
  //By default veil implements: bool, int, float, vec2, vec3, vec4, mat3, mat4, sampler2d
  //If the built-in types don't match your needs, you may register your own ones
  //Types registered by other mods must begin with "modid:"
  "type": "vec3",
  //Required
  //The beginning value of the property, it must fit the type, ie:
  //floats are single numbers ( 1.52 ), vectors are arrays ( [1.2, 1.3] )
  //matrices are arrays of arrays ( [[1.2, 0 ,0], [0, 1.2, 0], [0, 0, 1.2]] )
  //and sampler2d s are textures ( "veil:textures/foo/bar.png" )
  "value": [0.3, 0.6, 0.9]
}
```

#### Property Modifiers

Flare allows for dynamically manipulating effects.
Property modifiers accept a value from a controller, evaluate it in a curve (or curves),
and append the result it in a property.

```json5
{
  //Required
  "name": "modifierName",
  //Optional
  //The class the modifier should apply to.
  //If this field isn't present or the output property holds model data,
  //the modifier will apply to all classes in the effect.
  "class": "plumeA",
  //Required
  //Input controller
  "controller": "throttle",
  //Required
  //Output property
  "property": "Speed",
  //Required
  //Type, must fit the property's type
  //By default veil implements: float, vec2, vec3, vec4
  //As with properties, mods may register their own modifier type
  //and prefix them here with "modid:"
  "type": "float",
  //Required
  //Each member of the array specifies: the time of the node, the value of the node,
  //and the easing function used to interpolate to the other node
  //Types like vec2, vec3 or vec4 have this field replaced with "curves".
  //"curves" is an array of curves, or in other words, an array of arrays of nodes.
  "curve": [
    {"time": 0.0, "value": 0.0, "easing": "ease_out_quad"},
    {"time": 0.05, "value": 0.3, "easing": "ease_in_quad"},
    {"time": 1.0, "value": 1.0, "easing": "linear"}
  ],
  //The mode in which to append the new value: replace, add, subtract, multiply.
  "mode": "replace"
}
```

#### Host-Bound Controllers and Global Controllers

When rendering an effect, an [`EffectHost`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/flare/EffectHost.java) has to be present,
host-bound controllers collect data from these hosts to use it to evaluate `Property Modifiers`.
To do this, the `EffectHost#getValue` method is called with the name of the controller.

Global controllers on the other hand, do not use a host, they gather their data locally.
Veil has an event for registering custom global controllers.
Global controllers are automatically prefixed with `global::` when registering them.

### Materials

Materials describe the render type and uniforms used when rendering.

```json5
{
  //Required
  //Used to decide whether a Property Modifier should apply here or not
  "class": "plumeA",
  //Required
  //The veil render type used to render things with the material
  "renderType": "veil:rendertype",
  //Optional
  //Whether the built in "_Seed" property should be added
  "randomizeSeed": false,
  //Required
  //Properties, will be applied as shader uniforms when rendering with this material
  "properties": [
    //...
  ]
}
```

### Models

Models hold a shell, transformations that should be applied to it during rendering,
and one or more materials that render that shell.

`positionOffset`, `rotationOffset`, and `scaleOffset` are all properties that can be manipulated by `Property Modifiers`,
Called `model::position`, `model::rotation`, and `model::scale` respectively.

Additionally, another model property is added, `ModelToWorld` - this is a `mat4` property that holds the transformation from
model space to world space.
When this property is applied as a uniform, its inverse, `IModelToWorld` is also applied.

```json5
{
  //Required
  //Reference to a shell
  "path": "veil:cube",
  //Required
  //Position offset
  //Also added as a model property called "model::position"
  "positionOffset": [0, 0, 0],
  //Required
  //XYZ Rotation Offset in Degrees
  //Also added as a model property called "model::rotation"
  "rotationOffset": [0, 0, 0],
  //Required
  //Scale offset
  //Also added as a model property called "model::scale"
  "scaleOffset": [1, 1, 1],
  //Required
  //Single material OR array of materials
  "materials": {
    //...
  }
}
```

### Effect Layer

Effect layers have a name and a model,
they also have the list of `Property Modifiers` that should be applied when rendering.

```json5
{
  //Required
  //Name of the layer
  "name": "name",
  //Optional
  //Whether the layer should at all be rendered. Useful for debugging
  "disabled": false,
  //Required
  //Single model ONLY
  "model": {
    //...
  },
  //Required
  //Property modifiers
  "modifiers": [
    //...
  ]
}
```

### Effect Template

Effect templates simply are a list of `Effect Layers`.
Effect templates are actual files, and are placed in `flare/templates/`.

```json5
{
  //Required
  //List of Effect Layers
  "layers": [
    //...
  ]
}
```

### Modules

Final form of effects.
Modules are files placed in `flare/module/`.
Module have multiple `sub modules`.

```json5
{
  //Required
  "subModules": {
    //Sub module consisting of a single template
    "plume": "veil:plume",
    //Sub module consisting of one or more templates
    "splash": [
      "veil:splash",
      "veil:plume"
    ]
  }
}
```

When rendering modules you are given the decision of what sub module to render,
the sub module you chose will then render all templates it consists of.

### Shell Overrides

When rendering, you are given the option to override the shells in the templates that are being rendered,
that way effects can have dynamic and custom models.

### Rendering

Rendering effects is as simple as:

```java
public static void renderEffect(...) {
    ResourceLocation module = ...;
    String subModule = ...;
    EffectHost host = ...;
    MatrixStack matrixStack = ...;
    float partialTick = ...;
    try {
        FlareEffectManager.getModule(module).getSubModule(subModule).render(host, matrixStack, partialTick);
    } catch (Exception ignored) {
        
    }
}
```

Templates can also be rendered directly:

```java
public static void renderEffect(...) {
    ResourceLocation template = ...;
    EffectHost host = ...;
    MatrixStack matrixStack = ...;
    float partialTick = ...;
    try {
        FlareEffectManager.getTemplate(template).render(host, matrixStack, partialTick);
    } catch (Exception ignored) {
        
    }
}
```

### Built in Properties and Global Controllers

#### Properties

Materials with the `randomizeSeed` field set to `true` are added the `_Seed` property,
a property with a random value from 0..1. This property cannot be modified.

All materials have the `_Time` property added to them.
`(t/2, t, t*2, t*3)` is the vector associated with the property,
with `t` being the time in **seconds** since Minecraft has launched.

#### Controllers

Veil registers the `random` controller, a controller with a random value from 0..1 each time its value is requested.

*(Don't forget! Because global controllers are prefixed with "global::" when registered,
the "random" controller should be accessed with "global::random")*
