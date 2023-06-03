package foundry.veil.anim

import net.minecraft.world.phys.Vec3

abstract class Frame(open var pos: Vec3) {

    fun getPos(): Vec3 {
        return pos
    }

    fun setPos(pos: Vec3) {
        this.pos = pos
    }

    fun interpolate(frame: Frame, progress: Float): Frame {
        val x = pos.x + (frame.pos.x - pos.x) * progress
        val y = pos.y + (frame.pos.y - pos.y) * progress
        val z = pos.z + (frame.pos.z - pos.z) * progress
        return KeyFrame(Vec3(x, y, z), 0)
    }
}