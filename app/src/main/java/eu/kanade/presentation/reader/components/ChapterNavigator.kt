package eu.kanade.presentation.reader.components

enum class ChapterNavigatorType {
    HORIZONTAL_LTR,
    HORIZONTAL_RTL,
    VERTICAL_LEFT,
    VERTICAL_RIGHT,
    ;

    fun isHorizontal() = this in setOf(HORIZONTAL_LTR, HORIZONTAL_RTL)

    /** Whether the edge scrubber sits on the physical left edge of the screen. */
    fun scrubberOnLeft() = this == HORIZONTAL_RTL || this == VERTICAL_LEFT
}
