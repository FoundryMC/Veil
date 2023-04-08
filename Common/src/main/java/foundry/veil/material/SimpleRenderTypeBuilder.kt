package foundry.veil.material

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import foundry.veil.extension.lightmapShard
import foundry.veil.extension.textureShard
import foundry.veil.material.SimpleRenderTypeBuilder.Companion.renderType
import net.minecraft.client.renderer.RenderStateShard.*
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation

class SimpleRenderTypeBuilder {
    private var builder: RenderType.CompositeState.CompositeStateBuilder = RenderType.CompositeState.builder()
    var textureShard: TextureStateShard?
        get() = null
        set(value) {
            texture(value!!)
        }
    var transparencyShard: TransparencyStateShard?
        get() = null
        set(value) {
            transparency(value!!)
        }
    var depthShard: DepthTestStateShard?
        get() = null
        set(value) {
            depth(value!!)
        }
    var cullShard: CullStateShard?
        get() = null
        set(value) {
            cull(value!!)
        }
    var lightShard: LightmapStateShard?
        get() = null
        set(value) {
            light(value!!)
        }
    var overlayShard: OverlayStateShard?
        get() = null
        set(value) {
            overlay(value!!)
        }
    var layerShard: LayeringStateShard?
        get() = null
        set(value) {
            layer(value!!)
        }
    var outputShard: OutputStateShard?
        get() = null
        set(value) {
            output(value!!)
        }
    var texturingShard: TexturingStateShard?
        get() = null
        set(value) {
            texturing(value!!)
        }
    var writeMaskShard: WriteMaskStateShard?
        get() = null
        set(value) {
            writeMask(value!!)
        }
    var lineShard: LineStateShard?
        get() = null
        set(value) {
            line(value!!)
        }

    var shaderShard: ShaderStateShard?
        get() = null
        set(value) {
            shader(value!!)
        }

    fun shader(value: ShaderStateShard) {
        shaderShard = value
    }

    var vertexFormat: VertexFormat
        get() = DefaultVertexFormat.POSITION_COLOR_TEX
        set(value) {
            vertexFormat(value)
        }
    var vertexMode: VertexFormat.Mode
        get() = VertexFormat.Mode.QUADS
        set(value) {
            vertexMode(value)
        }
    var bufferSize: Int
        get() = 256
        set(value) {
            bufferSize(value)
        }
    var name: String
        get() = "simple_render_type_builder"
        set(value) {
            name(value)
        }
    var outline: Boolean
        get() = false
        set(value) {
            outline(value)
        }

    init {
        vertexFormat = DefaultVertexFormat.POSITION_COLOR_TEX
        vertexMode = VertexFormat.Mode.QUADS
        bufferSize = 256
        name = "simple_render_type_builder"
    }

    fun build(): RenderType {
        if (textureShard != null) textureShard?.let { builder.setTextureState(it) }
        if (transparencyShard != null) transparencyShard?.let { builder.setTransparencyState(it) }
        if (depthShard != null) depthShard?.let { builder.setDepthTestState(it) }
        if (cullShard != null) builder.setCullState(cullShard)
        if (lightShard != null) builder.setLightmapState(lightShard)
        if (overlayShard != null) builder.setOverlayState(overlayShard)
        if (layerShard != null) builder.setLayeringState(layerShard)
        if (outputShard != null) outputShard?.let { builder.setOutputState(it) }
        if (texturingShard != null) builder.setTexturingState(texturingShard)
        if (writeMaskShard != null) builder.setWriteMaskState(writeMaskShard)
        if (lineShard != null) builder.setLineState(lineShard)
        if (shaderShard != null) builder.setShaderState(shaderShard)
        return RenderType.create(
            name,
            vertexFormat,
            vertexMode,
            256,
            false,
            true,
            builder.createCompositeState(outline)
        )
    }

    companion object {
        fun renderType(builder: SimpleRenderTypeBuilder.() -> Unit): SimpleRenderTypeBuilder {
            val simpleRenderTypeBuilder = SimpleRenderTypeBuilder()
            simpleRenderTypeBuilder.builder()
            return simpleRenderTypeBuilder
        }
    }

    // dsl
    fun texture(texture: TextureStateShard) {
        this.textureShard = texture
    }

    fun transparency(transparency: TransparencyStateShard) {
        this.transparencyShard = transparency
    }

    fun depth(depth: DepthTestStateShard) {
        this.depthShard = depth
    }

    fun cull(cull: CullStateShard) {
        this.cullShard = cull
    }

    fun light(light: LightmapStateShard) {
        this.lightShard = light
    }

    fun overlay(overlay: OverlayStateShard) {
        this.overlayShard = overlay
    }

    fun layer(layer: LayeringStateShard) {
        this.layerShard = layer
    }

    fun output(output: OutputStateShard) {
        this.outputShard = output
    }

    fun texturing(texturing: TexturingStateShard) {
        this.texturingShard = texturing
    }

    fun writeMask(writeMask: WriteMaskStateShard) {
        this.writeMaskShard = writeMask
    }

    fun line(line: LineStateShard) {
        this.lineShard = line
    }

    fun vertexFormat(vertexFormat: VertexFormat) {
        this.vertexFormat = vertexFormat
    }

    fun vertexMode(vertexMode: VertexFormat.Mode) {
        this.vertexMode = vertexMode
    }

    fun bufferSize(bufferSize: Int) {
        this.bufferSize = bufferSize
    }

    fun name(name: String) {
        this.name = name
    }

    fun outline(outline: Boolean) {
        this.outline = outline
    }
}