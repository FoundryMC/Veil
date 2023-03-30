package foundry.veil.extensions

import com.mojang.blaze3d.vertex.PoseStack

fun PoseStack.moveTo(x: Double, y: Double, z: Double) {
    this.last().pose().m03 = x.toFloat()
    this.last().pose().m13 = y.toFloat()
    this.last().pose().m23 = z.toFloat()
}