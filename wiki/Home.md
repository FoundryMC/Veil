# Overview

All veil resources should be located in the `assets/modid/pinwheel` folder. Specific assets are in subfolders of the
main pinwheel folder.

The exception is Quasar particles, which are located in the `assets/modid/quasar` folder.

# Getting Started

The latest version can be found in the Veil [README](https://github.com/FoundryMC/Veil/blob/1.20/README.md) or
directly from [Jared's Maven](https://maven.blamejared.com/foundry/veil/).

### Neoforge

<details>
  <summary>Click to expand</summary>

```groovy
repositories {
    maven {
        name = 'BlameJared Maven (CrT / Bookshelf)'
        url = 'https://maven.blamejared.com'
    }
}

dependencies {
    implementation("foundry.veil:veil-neoforge-${project.minecraft_version}:${project.veil_version}") {
        exclude group: "maven.modrinth"
        exclude group: "me.fallenbreath"
    }
}
```

</details>

### Fabric

<details>
  <summary>Click to expand</summary>

```groovy
repositories {
    maven {
        name = 'BlameJared Maven (CrT / Bookshelf)'
        url = 'https://maven.blamejared.com'
    }
}

dependencies {
    modImplementation("foundry.veil:veil-fabric-${project.minecraft_version}:${project.veil_version}") {
        exclude group: "maven.modrinth"
        exclude group: "me.fallenbreath"
    }
}
```

</details>

### Common

<details>
  <summary>Click to expand</summary>

```groovy
repositories {
    maven {
        name = 'BlameJared Maven (CrT / Bookshelf)'
        url = 'https://maven.blamejared.com'
    }
}

dependencies {
    implementation("foundry.veil:veil-common-${project.minecraft_version}:${project.veil_version}") {
        exclude group: "maven.modrinth"
        exclude group: "me.fallenbreath"
    }
}
```

</details>

# Veil Features

- [Veil Events](Events)
- [Better Vertex Buffers](VertexArray)
- [Shader Modifications](ShaderModification)
- [Dynamic Buffers](DynamicBuffer.md)
- [Custom Shaders](Shader)
- [Custom Framebuffers](Framebuffer)
- [Data-Driven Render Types](CustomRenderType)
- [Render Type Stages](RenderTypeStage)
- [Post-Processing](PostProcessing)
- [Animations](Animations)
- [Colors](Colors)
- [Easings](Easings)
- [Poses and custom code-based Animations](Poses)
- [Quasar](Quasar)
- [Flare](Flare)
