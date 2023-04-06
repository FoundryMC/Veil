package foundry.veil.extension

import net.minecraft.client.renderer.RenderStateShard

class DepthTestStateShardBuilder {
    var name:String
        get() = ""
        set(value) {
            name(value)
        }
    private fun name(value: String) = apply { name = value }
    var func: Int
        get() = 0
        set(value) {
            func(value)
        }
    private fun func(value: Int) = apply { func = value }

    fun build(): RenderStateShard.DepthTestStateShard {
        return RenderStateShard.DepthTestStateShard(name, func)
    }
}

fun depthTestShard(init: DepthTestStateShardBuilder.() -> Unit): RenderStateShard.DepthTestStateShard {
    val depthTestStateShard = DepthTestStateShardBuilder()
    depthTestStateShard.init()
    return depthTestStateShard.build()
}