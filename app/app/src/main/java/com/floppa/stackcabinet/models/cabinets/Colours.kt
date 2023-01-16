package com.floppa.stackcabinet.models.cabinets

import androidx.compose.ui.graphics.Color

/**
 * Colors of the Cabinets
 * https://www.flatuicolorpicker.com/
 */
enum class Colors(val color: Color) {
    RED(Color.hsv (1f,0.88f,0.85f)),
    ORANGE(Color.hsv(23f,0.94f,0.97f)),
    SILVER(Color.hsv (204f,0.5f,0.78f)),
    YELLOW(Color.hsv (47f,0.90f,0.96f)),
    PURPLE(Color.hsv (272f,0.75f,0.54f)),
    PINK(Color.hsv (328f,0.73f,0.89f))
}