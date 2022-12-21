package foundry.veil.gara

import foundry.veil.color.Color

/**
 * Stores 4 colors, representing the edges of a box
 */
class EdgeColors(val top: Color, val right: Color, val bottom: Color, val left: Color) {
    constructor(uniform: Color) : this(uniform, uniform, uniform, uniform)
}