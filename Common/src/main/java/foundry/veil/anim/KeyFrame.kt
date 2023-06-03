package foundry.veil.anim

import net.minecraft.world.phys.Vec3

class KeyFrame(override var pos: Vec3, var duration: Int) : Frame(pos) {
}