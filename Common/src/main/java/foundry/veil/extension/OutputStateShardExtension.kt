package foundry.veil.extension

import net.minecraft.client.renderer.RenderStateShard


class OutputStateShardBuilder {
    var name:String
        get() = ""
        set(value) {
            name(value)
        }
    private fun name(value: String) = apply { name = value }
    var setupState: Runnable
        get() = Runnable {  }
        set(value) {
            setupState(value)
        }
    private fun setupState(value: Runnable) = apply { setupState = value }

    var clearState:Runnable
        get() = Runnable {  }
        set(value) {
            clearState(value)
        }
    private fun clearState(value: Runnable) = apply { clearState = value }

    fun build(): RenderStateShard.OutputStateShard {
        return RenderStateShard.OutputStateShard(name, setupState, clearState)
    }
}

fun outputShard(init: OutputStateShardBuilder.() -> Unit): RenderStateShard.OutputStateShard {
    val outputStateShardBuilder = OutputStateShardBuilder()
    outputStateShardBuilder.init()
    return outputStateShardBuilder.build()
}