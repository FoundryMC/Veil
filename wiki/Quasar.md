Quasar is an advanced particle system that is fully data-driven. Instead of defining individual particles, Quasar instead defines particle _emitters_ which allow for multiple particles to be emitted at once/over time. These particles have modules attached to them, which allow them to have more complex properties. There are many modules available as base building blocks, but more can be implemented using Java. Quasar allows for significantly more flexibility in comparison to vanilla particles, as well as easier hot-swapping and registration.

# Key concepts

❗ These paths go off of `assets/modid/quasar`.

### Particle Emitters

| Folder | `emitters`                                                                                                                           |
|--------|--------------------------------------------------------------------------------------------------------------------------------------|
| Codec  | [Github](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/quasar/data/ParticleEmitterData.java#L27) |

Particle emitters are the starting point for everything related to Quasar's particles. They define a few variables about
themselves like lifetime and the rate at which they emit particles, but more importantly they hold the links to the
Particle Data and Shape that each describe specific properties of the spawned particles.

Syntax:
```json5
{
	// Required
	// How long the emitter emits particles, in ticks
	"max_lifetime": 20,
	// Optional
	// Whether the emitter will reset after max_lifetime ticks
	"loop": true,
	// Required
	// The delay between particles being emitted, in ticks
	"rate": 5,
	// Required
	// How many particles are emitted every [rate] ticks
	"count": 2,
	// Required
	// The settings that define how to emit the particles
	"emitter_settings": "modid:path/to/emitter/settings",
	// Required
	// The data that define how each particle behaves
	"particle_data": "modid:path/to/particle/data"
}
```

### Emitter Settings

| Folder | `modules/emitter/particle`                                                                                                        |
|--------|-----------------------------------------------------------------------------------------------------------------------------------|
| Codec  | [Github](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/quasar/data/ParticleSettings.java#L26) |

Sets lifetime, size, speed, initial rotation and the ranges for randomizing them.

Syntax (note that all fields are required, none are optional):
```json5
{
  // Whether the speed should be randomized. The range of the speed is between 0.5x and 1.5x the value of particle_speed
  "random_speed": false,
  // Whether the size of each particle should be randomized. The range of the size is between base_particle_size and base_particle_size + particle_size_variation
  "random_size": false,
  // Whether the lifetime of each particle should be randomized. The range of the lifetime is between particle_lifetime and particle_lifetime + particle_lifetime_variation.
  "random_lifetime": false,
  // Whether the initial direction of each particle should be randomized. The range of the direction, for each axis, is between -value and value.
  "random_initial_direction": true,
  // Currently unused. Use the init_random_rotation module.
  "random_initial_rotation": false,
  // The inital direction of each particle, potentially modified by random_direction.
  "initial_direction": [
    1.0,
    1.0,
    1.0
  ],
  "particle_size_variation": 0.0075,
  "particle_lifetime": 60,
  "particle_lifetime_variation": 0.1,
  "particle_speed": 0.1,
  "base_particle_size": 0.1
}
```

### Emitter Shapes

| Folder | `modules/emitter/shape`                                                                                           |
|--------|-------------------------------------------------------------------------------------------------------------------|
| Shapes | [Github](https://github.com/FoundryMC/Veil/tree/1.21/common/src/main/java/foundry/veil/api/quasar/emitters/shape) |

A shape describes where relative to the location of the Particle Emitter to spawn the individual particles.

Possible Shapes:
- `veil:point` or `POINT`
- `veil:hemisphere` or `HEMISPHERE`
- `veil:cylinder` or `CYLINDER`
- `veil:sphere` or `SPHERE`
- `veil:cube` or `CUBE`
- `veil:torus` or `TORUS`
- `veil:disc` or `DISC`
- `veil:plane` or `PLANE`

Syntax (all fields are required):
```json5
{
  "shape": "veil:cube",
  // Dimensions, in blocks
  "dimensions": [1.5, 2.0, 1.5],
  // Rotation, in degrees
  "rotation": [45, 45, 45],
  // Whether the particles should be emitted from the surface of the shape or a random point within the shape
  "from_surface": true
}
```

### Modules

| Folder | `modules`                                                                                                      |
|--------|----------------------------------------------------------------------------------------------------------------|
| Link   | [Github](https://github.com/FoundryMC/Veil/tree/1.21/common/src/main/java/foundry/veil/api/quasar/data/module) |

Modules are the most powerful building block of Quasar. A module is code attached to a particle, where it is executed at
a specific time, depending on which kind of module it is. They make particles dynamic by defining movement,
color changes or light emission.

The simplest module definition would look like this:
```json5
{
  // Required
  // The name of the module
  "module": "die_on_collision"
}
```

Modules are interacted with by making a json for the Module instance, telling it which `module` to use and adding the
parameters the module takes in as further fields.

We'll talk more about the individual types of modules later in this article.

### Particle Data

| Folder | `modules/particle_data`                                                                                                             |
|--------|-------------------------------------------------------------------------------------------------------------------------------------|
| Codec  | [Github](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/quasar/data/QuasarParticleData.java#L52) |

Defines the properties attached to each particles. Includes any modules attached to the particle, how it is rendered, and various other properties. 

Syntax:
```json5
{
  // Required - all other fields are optional
  // The render style can be either "CUBE" or "BILLBOARD".
  // "BILLBOARD" is meant for particles with textures that always face the player, whereas "CUBE" displays colored, textureless cubes.
  "render_type": "CUBE",
  // Init modules run once on a particle when it is spawned.
  "init_modules": [],
  // Update modules run every tick while the particle is alive.
  "update_modules": [],
  // Collision modules run once when the particle collides with something.
  "collision_modules": [],
  // Force modules also run every tick while the particle is alive and apply different changes to velocity based on module.
  "forces": [],
  // Render modules run every frame and can change how the particle appears.
  "render_modules": [],
  // Holds all information related to the sprite
  "sprite_data": {
    // Required
    // Path towards sprite texture
    // Base path is assets/modid, you will have to write /textures/particle/ if you want to use the same folder as vanilla.
    "sprite": "modid:path/to/particle/sprite",
    // Optional
    // Amount of frames in the animation (default 1)
    "frame_count": 2,
    // Optional
    // The time that one frame lasts for, in ticks
    "frame_time": 2.0,
    // Optional
    // The width of one frame, in pixels
    "frame_width": 16,
    // Optional
    // The height of one frame, in pixels
    "frame_height": 16,
    // Optional
    // If true, makes the animation stretch to play once throughout the lifetime of the particle. Disregards frame_time.
    // If false, the particle animation will loop.
    "stretch_to_lifetime": true,
  },
  // Whether the transparency is additive or not.
  "additive": false,
  // Whether the particle should interact with collisions or not.
  "should_collide": true,
  // Whether the particle should face the direction of its velocity.
  "face_velocity": false,
  // How much the particle should stretch based on its velocity. 0 (the default) disables velocity stretching.
  "velocity_stretch_factor": 1.0
}
```

# Working with Quasar

### Codecs:

Quasar uses codecs to read its particles from resource packs. If you are not familiar with what a codec is,
read up [here](https://gist.github.com/Drullkus/1bca3f2d7f048b1fe03be97c28f87910) or, if you are more familiar with
Java, [here (Fabric)](https://docs.fabricmc.net/develop/codecs) or [here (NeoForge)](https://docs.neoforged.net/docs/datastorage/codecs).

When you don't know what fields an entry may have, search it up on [Veil's Github](https://github.com/FoundryMC/Veil) or, if it is a Minecraft codec, use [Linkie](https://linkie.shedaniel.dev/mappings?namespace=mojang_raw&version=1.21.1&search=&translateMode=none).

### Particle Editor

Not currently finished! Please return at a later point for documentation and use a text editor
of your choice for now.

### Resource Browser

The Veil text editor is disabled in the current version. Please use an external editor with hotswapping until it is re-implemented.

~~Veil has a resource browser that allows you to edit any text file from any resource pack in-game and save it. It can be
opened by pressing the (by default) F6 key and clicking on the `Resource` Tab. Files can be edited by clicking on them
with the Right mouse button, which will open a context window with the `Open in Veil Text Editor` option. This will open
a Code editor layered over Minecraft. When you have edited the file and want to apply the changes, save it using the
editor and either press `F3+t` or use the `Reload resources` button at the top of the resource browser window to reload
all resource packs.~~

# Modules

All available modules and their IDs for use in the Particle Module data can be
found [here.](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/quasar/data/ParticleModuleTypeRegistry.java#L41-L71)

We'll now go over some example modules to showcase the modules' structure. You've already seen the module `die_on_collision`, which kills the particle when it collides with the world.

The first module we'll look at is `wind`, a force module. All fields are required. You can cross reference the [raw codec](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/quasar/data/module/force/WindForceData.java#L32-L34) to see how the properties line up.
```json5
{
  "module": "wind",
  // This vector will be normalized.
  "wind_direction": [1.0, 0.0, 0.0],
  "wind_speed": 2.0,
  // Strength is unused.
  "strength": 1.0
}
```
This will make all particles blow towards the positive X axis, with a strength of 2.0.

The other module we'll look at is `color`, which adds a color gradient to each particle. Again, please reference the [codec](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/quasar/data/module/render/ColorParticleModuleData.java#L19-L20) to see how it corresponds to the properties of the module.

This module uses Molang to determine where along the gradient the particle is. Explaining Molang is beyond the scope of this article, but there is a good reference [here](https://bedrock.dev/docs/stable/Molang). You can see a list of available queries [here](https://github.com/FoundryMC/Veil/blob/eb13366df06eda66054ee783f5abf956f4581d1a/common/src/main/java/foundry/veil/api/quasar/particle/QuasarParticle.java#L72-L88).
```json5
{
  "module": "color",
  "gradient": {
    "rgb_points": [
      {
        "percent": 0.0,
        "color": [
          255, 255, 255
        ]
      },
      {
        "percent": 1.0,
        "color": [
          255, 0, 255
        ]
      }
    ],
    "alpha_points": [
      {
        "percent": 0.0,
        "alpha": 1
      },
      {
        "percent": 1.0,
        "alpha": 0
      }
    ]
  },
  "interpolant": "q.agePercent"
}
```

### Creating Custom Modules

In case we've lost you somewhere between Particle Module Data, Module Data and Modules, here's the terminology:

**Module Data**: JSON-defined parameters for a specific Module. The corresponding Java record handles applying modules
to the particle based on the data from the Json.

**Module**: Interface-implementing class that takes values from the Module Data to use them in logic executed on the
particle (`init`, `update`, `render`).

**Particle Module Data**: Defines the modules to attach to the particle.

```mermaid
flowchart
	emitter[Particle Emitter]
data[Particle Module data
=> List of Modules]
pmodules[Particle Modules
Run every frame/tick]
particle[Individual Particle]
shape[Shape]
modules[Modules]
moduledata[Module Data
=> Parameters for the Module]
settings[Particle Settings
=> Lifetime and size]
emitter--spawns using-->shape-->particle
emitter--supplies-->settings-->particle
moduledata-->modules
emitter--uses-->data--to apply-->modules--to -->particle
particle-->pmodules-->particle
```

`ParticleModuleData.addModules` is called on the data of every module applied to each particle when each particle is
spawned and can be used to add different modules based on the module data. For example, there's only a `LightModuleData`
which, depending if a color or alpha gradient is defined, either adds a `StaticLightModule` or a `DynamicLightModule` to
the particle to save calculations.

The module itself consists of a class that implements the `ParticleModule` interface for the particle's lifecycle:
`Init`, `Update`, `Collision`, `Force`, and `Render` and gets passed the values that were defined in its Module Data
file.

Each Module Type has to be registered in the [`ParticleModuleTypeRegistry`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/quasar/data/ParticleModuleTypeRegistry.java)
so that Veil knows how to read it from the Module Data files.

# Getting started

### Setting up the resource pack

(please note that if you are making a mod, this is unnecessary; you can simply put the quasar folder with all your other assets)

Let's start by creating a resource pack with a folder structure for our particle in the `resourcepacks` folder of a
Minecraft Instance running Veil:

```markdown
resourcepacks
\-particles
  |-pack.mcmeta
    \-assets
      \-modid
        \-quasar
          |-emitters
          |-modules
            |-render
            |-update
            |-init
            |-force
            |---collision
            \-particle_data
              \-emitter
                \-particle
                  \-shape
```

Note that you have to replace `modid` with the ID of the mod you want to add particles for.

For your pack.mcmeta you can just put something like this where pack format 34 is for Minecraft
1.21.1. [For other versions, look here](https://minecraft.wiki/w/Pack_format#resource_pack_format_history)

```json
{
  "pack": {
    "pack_format": 34,
    "description": "resource pack for testing Quasar"
  }
}
```

### Making a particle

In this section I would go through all the files of a full particle definition and explain each file on an Example

### Spawning particles

#### Java

Since Quasar uses resource packs for its particles, it is suggested to make the code for spawning particles
fault-tolerant. If this code was not wrapped in a `try`-`catch` block, it would result in a crash if an invalid particle was attempted to be spawned.

```java
public static void spawnParticle(Entity entity, resourceLocation id){
    try {
        ParticleSystemManager manager = VeilRenderSystem.renderer().getParticleManager();
        ParticleEmitter emitter = manager.createEmitter(id);
        emitter.setAttachedEntity(entity);
        manager.addParticleSystem(emitter);
    } catch (Exception ignored) {

    }
}
public static void spawnParticle(Vec3 position, resourceLocation id){
    try {
        ParticleSystemManager manager = VeilRenderSystem.renderer().getParticleManager();
        ParticleEmitter emitter = manager.createEmitter(id);
        emitter.setPosition(position);
        manager.addParticleSystem(emitter);
    } catch (Exception ignored) {

    }
}
```

#### Commands

You can also use the `/quasar` command to spawn the particle. Its syntax is `/quasar <particleemitter> <position>`.
Please note that this command only exists client-side and therefore cannot be executed by anything other than the
player directly.

[//]: # (Should I go over creating a new module as an example step-by-step? Don't think so since the people that want to make that complex particles are probably more experienced in Java)
