package foundry.veil.anim

import foundry.veil.math.Easings.Easing
import net.minecraft.world.phys.Vec3

class KeyFrame(override var pos: Vec3, var duration: Int, var easing: Easing) : Frame(pos) {
}