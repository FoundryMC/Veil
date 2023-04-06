package foundry.veil.extension

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import java.util.*

fun TextureStateShard.texture(texture: ResourceLocation) {
    this.texture = Optional.of(texture)
}

fun TextureStateShard.blur(blur: Boolean) {
    this.blur = blur
}

fun TextureStateShard.mipmap(mip: Boolean) {
    this.mipmap = mip
}

fun textureShard(init: TextureStateShardBuilder.() -> Unit): TextureStateShard {
    val textureStateShard = TextureStateShardBuilder()
    textureStateShard.init()
    return textureStateShard.build()
}

class TextureStateShardBuilder() {
    var texture: ResourceLocation?
        get() = null
        set(value) {
            texture(value!!)
        }

    private fun texture(value: ResourceLocation) {
        texture = value
    }

    var blur: Boolean
        get() = false
        set(value) {
            blur(value)
        }

    private fun blur(value: Boolean) {
        blur = value
    }

    var mipmap: Boolean
        get() = false
        set(value) {
            mipmap(value)
        }

    private fun mipmap(value: Boolean) {
        mipmap = value
    }

    fun build(): TextureStateShard {
        return TextureStateShard(texture!!, blur, mipmap)
    }
}