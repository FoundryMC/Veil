package foundry.veil.anim

import foundry.veil.math.Easings.Easing
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3

open class Frame(open var pos: Vec3) {

    fun interpolate(frame: Frame, progress: Float, easing: Easing): Frame {
        val x = Mth.lerp(easing.ease(progress).toDouble(), pos.x, frame.pos.x)
        val y = Mth.lerp(easing.ease(progress).toDouble(), pos.y, frame.pos.y)
        val z = Mth.lerp(easing.ease(progress).toDouble(), pos.z, frame.pos.z)
        return Frame(Vec3(x, y, z))
    }

    fun x(): Double {
        return pos.x
    }

    fun y(): Double {
        return pos.y
    }

    fun z(): Double {
        return pos.z
    }
}