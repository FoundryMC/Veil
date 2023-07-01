//package foundry.veil.gara
//
//import foundry.veil.color.Color
//
///**
// * Base UI element in Gara UI, top of inheritance for all UI elements.
// * Contains common properties of all UI components.
// */
//class UIElement {
//
//    /**
//     * The display mode of a UI Element, directing how it and sized automatically and positioned in flow
//     */
//    enum class Display {
//        /**
//         * This element will be sized according to its contents with full width, positioned by the allocated space of the parent
//         */
//        BLOCK,
//
//        /**
//         * This element will ignore all size overrides and constraints, instead fitting the contents and
//         */
//        INLINE,
//
//        /**
//         * This element will be positioned and sized along the axis of the parent, based on the flex property
//         */
//        FLEX
//    }
//
//    /**
//     * The position resolution mode of a UI Element, directing how it is incorporated into the flow of a page
//     */
//    enum class Position {
//        /**
//         * Normal positioning, directed by the parent element
//         */
//        STATIC,
//
//        /**
//         * This element will be positioned at a provided position and size relative to the upmost element, with no input from the layout engine of the parent
//         */
//        ABSOLUTE,
//
//        /**
//         * This element will be positioned at a provided position and size relative to this elements parent, with no input from the layout engine of the parent
//         */
//        RELATIVE
//    }
//
//    /**
//     * All children of this UI element
//     */
//    var children: MutableList<UIElement> = mutableListOf<UIElement>()
//
//    /**
//     * Background color, supports [CornerColors] gradients
//     */
//    var background: CornerColors = CornerColors(Color.BLACK)
//
//
//    /**
//     * Default text color for all child text elements
//     */
//    var textColor: Color = Color.WHITE
//
//    /**
//     * Border color, supporting [EdgeColors]
//     */
//    var border: EdgeColors = EdgeColors(Color.BLACK)
//
//    /**
//     * Current display mode. Default of [Display.BLOCK]
//     */
//    var display: Display = Display.BLOCK
//
//    /**
//     * Current positioning mode. Default of [Position.STATIC]
//     */
//    var position: Position = Position.STATIC
//
//    /**
//     * Margins, or reserved space around this element
//     */
//    var margin: EdgeValues = EdgeValues(0f)
//
//    /**
//     * Padding, or reserved space inside this element
//     */
//    var padding: EdgeValues = EdgeValues(0f)
//
//    /**
//     * The flex coefficient, or how much of the parent container this element wants to take up
//     * Two elements with the same flex coefficient will share equal space
//     * See [Display.FLEX]
//     */
//    var flex: Float = 0.0f
//
//}