# Overview

All Veil resources should be located in the `assets/modid/pinwheel` folder. Specific assets are in subfolders of the
main pinwheel folder.

There are two exceptions: [Quasar](Quasar) particles, which are located in the `assets/modid/quasar` folder, and [Flare](Flare) effects, which are located in the `assets/modid/flare` folder.

Within development environments, the mod ImGuiMC is used to view various pieces of information about the game, such as framebuffers or lights. This dependency is not needed prior to Veil `4.0.0`.

# Getting Started

The latest version for Veil can be found in the Veil [README](https://github.com/FoundryMC/Veil/blob/1.21/README.md) or directly from [Jared's Maven](https://maven.blamejared.com/foundry/veil/).

The latest version for ImGuiMC can be found from [RyanHCode's Maven](https://maven.ryanhcode.dev/#/releases/foundry/imguimc)

### NeoForge

<details>
  <summary>Click to expand</summary>

```groovy
repositories {
    maven { // For ImGuiMC
        url = "https://maven.ryanhcode.dev/releases"
        name = "RyanHCode Maven"
    }
    maven { // For Veil
        name = 'BlameJared Maven (CrT / Bookshelf)'
        url = 'https://maven.blamejared.com'
    }
}

dependencies {
    implementation("foundry.veil:veil-neoforge-${project.minecraft_version}:${project.veil_version}") {
        exclude group: "maven.modrinth"
        exclude group: "me.fallenbreath"
    }
    implementation("foundry.imguimc:imguimc-neoforge-${project.minecraft_version}:${project.imguimc_version}")
}
```

</details>

### Fabric

<details>
  <summary>Click to expand</summary>

```groovy
repositories {
    maven { // For ImGuiMC
        url = "https://maven.ryanhcode.dev/releases"
        name = "RyanHCode Maven"
    }
    maven { // For Veil
        name = 'BlameJared Maven (CrT / Bookshelf)'
        url = 'https://maven.blamejared.com'
    }
}

dependencies {
    modImplementation("foundry.veil:veil-fabric-${project.minecraft_version}:${project.veil_version}") {
        exclude group: "maven.modrinth"
        exclude group: "me.fallenbreath"
    }
    modImplementation("foundry.imguimc:imguimc-fabric-${project.minecraft_version}:${project.imguimc_version}")
}
```

</details>

### Common

<details>
  <summary>Click to expand</summary>

```groovy
repositories {
    maven { // For ImGuiMC
        url = "https://maven.ryanhcode.dev/releases"
        name = "RyanHCode Maven"
    }
    maven { // For Veil
        name = 'BlameJared Maven (CrT / Bookshelf)'
        url = 'https://maven.blamejared.com'
    }
}

dependencies {
    implementation("foundry.veil:veil-common-${project.minecraft_version}:${project.veil_version}") {
        exclude group: "maven.modrinth"
        exclude group: "me.fallenbreath"
    }
    implementation("foundry.imguimc:imguimc-common-${project.minecraft_version}:${project.imguimc_version}")
}
```

</details>

# Veil Features

- [Colors](Colors)
- [Data-Driven Render Types](CustomRenderType)
- [Dynamic Buffers](DynamicBuffer)
- [Easings](Easings)
- [Veil Events](Events)
- [Flare (Data-driven Effects)](Flare)
- [Custom Framebuffers](Framebuffer)
- [Necromancer (Animations)](Necromancer)
- [Post-Processing](PostProcessing)
- [Quasar (Particles)](Quasar)
- [Render Type Stages](RenderTypeStage)
- [Custom Shaders](Shader)
- [Shader Injections](ShaderInject)
- [Better Vertex Buffers](VertexArray)
