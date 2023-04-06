package foundry.veil.extension

import net.minecraft.client.renderer.RenderStateShard

class WriteMaskStateShardBuilder {
    var writeColor: Boolean
        get() = false
        set(value) {
            writeColor(value)
        }
    private fun writeColor(value: Boolean) = apply { writeColor = value }

    var writeDepth: Boolean
        get() = false
        set(value) {
            writeDepth(value)
        }
    private fun writeDepth(value: Boolean) = apply { writeDepth = value }

    fun build(): RenderStateShard.WriteMaskStateShard {
        return RenderStateShard.WriteMaskStateShard(writeColor, writeDepth)
    }
}

fun writeMaskShard(init: WriteMaskStateShardBuilder.() -> Unit): RenderStateShard.WriteMaskStateShard {
    val writeMaskStateShard = WriteMaskStateShardBuilder()
    writeMaskStateShard.init()
    return writeMaskStateShard.build()
}