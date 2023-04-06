package foundry.veil.extension

import net.minecraft.client.renderer.RenderStateShard
import java.util.OptionalDouble

class LineStateShardBuilder {
    var width: OptionalDouble
        get() = OptionalDouble.empty()
        set(value) {
            width(value)
        }
    private fun width(value: OptionalDouble) = apply { width = value }

    fun build(): RenderStateShard.LineStateShard {
        return RenderStateShard.LineStateShard(width)
    }
}

fun lineShard(init: LineStateShardBuilder.() -> Unit): RenderStateShard.LineStateShard {
    val lineStateShard = LineStateShardBuilder()
    lineStateShard.init()
    return lineStateShard.build()
}