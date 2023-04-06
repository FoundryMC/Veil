package foundry.veil.extension

import net.minecraft.client.renderer.RenderStateShard


class TransparencyStateShardBuilder {
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

    fun build(): RenderStateShard.TransparencyStateShard {
        return RenderStateShard.TransparencyStateShard(name, setupState, clearState)
    }
}

fun transparencyShard(init: TransparencyStateShardBuilder.() -> Unit): RenderStateShard.TransparencyStateShard {
    val transparencyStateShard = TransparencyStateShardBuilder()
    transparencyStateShard.init()
    return transparencyStateShard.build()
}