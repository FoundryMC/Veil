package foundry.veil.extension

import net.minecraft.client.renderer.RenderStateShard

class OverlayStateShardBuilder {
    var overlay: Boolean
        get() = false
        set(value) {
            overlay(value)
        }
    private fun overlay(value: Boolean) = apply { overlay = value }

    fun build(): RenderStateShard.OverlayStateShard {
        return RenderStateShard.OverlayStateShard(overlay)
    }
}

fun overlayShard(init: OverlayStateShardBuilder.() -> Unit): RenderStateShard.OverlayStateShard {
    val overlayStateShard = OverlayStateShardBuilder()
    overlayStateShard.init()
    return overlayStateShard.build()
}