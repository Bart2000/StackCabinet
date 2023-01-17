package com.floppa.stackcabinet.models

import androidx.compose.ui.graphics.Color

enum class CommandsEnum {
    REQUEST_GRID,
    SET_COMPONENT,
    SET_LED,
    OPEN_CABINET,
    RESET_ALL
}


class Commands {

    /**
     * Send command to request the Grid of the Cabinets from the ESP32
     */
    fun requestGrid(): String {
        return buildString {
            append(CommandsEnum.REQUEST_GRID.ordinal)
        }
    }

    /**
     * Send command to store the ID of a component to the cabinet
     * @param component component to store
     * @param cabinet cabinet where the component is stored
     */
    fun setComponent(component: Component, cabinet: Cabinet): String {
        return buildString {
            append(CommandsEnum.SET_COMPONENT.ordinal).append(",")
                .append(cabinet.id).append(",")
                .append(component.index)
        }
    }

    /**
     * Send command to Reset the ID of a component to the cabinet
     * @param cabinet cabinet that needs to be reset
     */
    fun resetComponent(cabinet: Cabinet): String {
        return buildString {
            append(CommandsEnum.SET_COMPONENT.ordinal).append(",")
                .append(cabinet.id).append(",")
                .append("0")
        }
    }

    /**
     * Send command to set the LED color and brightness
     * @param cabinet of which we want to control the LED'S of
     * @param color the color we want to set for that cabinet
     * @param brightness multiplier used to control the brightness, from 0.0f to 1.0f
     */
    fun setLed(cabinet: Cabinet, color: Color, brightness: Float): String {

        require(brightness in 0f..1.0f) {
            "Brightness should be in range of 0.0f to 1.0f"
        }

        return buildString {
            append(CommandsEnum.SET_LED.ordinal).append(",")
                .append(cabinet.id).append(",")
                .append((color.red * brightness)).append(",")
                .append((color.green * brightness)).append(",")
                .append((color.blue * brightness))
        }
    }

    /**
     * Send command to open a cabinet
     * @param cabinet that needs to be opened
     */
    fun openCabinet(cabinet: Cabinet): String {
        return buildString {
            append(CommandsEnum.OPEN_CABINET.ordinal).append(",")
                .append(cabinet.id)
        }
    }

    /**
     * Send command to rest all the cabinet.
     * By resetting we mean to remove the component ID from a cabinet
     */
    fun resetAll(): String {
        return buildString {
            append(CommandsEnum.RESET_ALL.ordinal)
        }
    }


}