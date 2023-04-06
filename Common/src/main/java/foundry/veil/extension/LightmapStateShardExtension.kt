package foundry.veil.extension

import net.minecraft.client.renderer.RenderStateShard

class LightmapStateShardBuilder {
    var lightmap: Boolean
        get() = false
        set(value) {
            lightmap(value)
        }
    private fun lightmap(value: Boolean) = apply { lightmap = value }

    fun build(): RenderStateShard.LightmapStateShard {
        return RenderStateShard.LightmapStateShard(lightmap)
    }
}

fun lightmapShard(init: LightmapStateShardBuilder.() -> Unit): RenderStateShard.LightmapStateShard {
    val lightmapStateShard = LightmapStateShardBuilder()
    lightmapStateShard.init()
    return lightmapStateShard.build()
}