package foundry.veil.extension

import net.minecraft.client.renderer.RenderStateShard

class CullStateShardBuilder {
    var cull: Boolean
        get() = false
        set(value) {
            cull(value)
        }
    private fun cull(value: Boolean) = apply { cull = value }

    fun build(): RenderStateShard.CullStateShard {
        return RenderStateShard.CullStateShard(cull)
    }
}

fun cullShard(init: CullStateShardBuilder.() -> Unit): RenderStateShard.CullStateShard {
    val cullStateShard = CullStateShardBuilder()
    cullStateShard.init()
    return cullStateShard.build()
}