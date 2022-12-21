package foundry.veil.gara

import foundry.veil.color.Color

/**
 * Stores 4 colors, representing the corners of a box
 */
class CornerColors(val topLeft: Color, val topRight: Color, val bottomLeft: Color, val bottomRight: Color) {
    constructor(uniform: Color) : this(uniform, uniform, uniform, uniform)
}