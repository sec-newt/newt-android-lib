package io.github.secnewt.ui

import androidx.annotation.DrawableRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Weather pieces shared by Newt Weather and Newt Home, so both draw the weather the same way.
 * Icons are Material Symbols (Rounded, filled; Apache License 2.0, Google).
 */
object NewtWeather {

    /**
     * The icon for a condition, by name as Newt Weather shares it (CLEAR, PARTLY_CLOUDY, CLOUDY,
     * RAIN, THUNDERSTORM, SNOW, FOG, WIND). Sun or moon follows [isDaytime]. Unknown names get
     * the cloud, so a newer Newt Weather never breaks an older reader.
     */
    @DrawableRes
    fun icon(condition: String?, isDaytime: Boolean): Int = when (condition) {
        "CLEAR" -> if (isDaytime) R.drawable.ic_wx_sunny else R.drawable.ic_wx_clear_night
        "PARTLY_CLOUDY" -> if (isDaytime) R.drawable.ic_wx_partly_cloudy_day else R.drawable.ic_wx_partly_cloudy_night
        "RAIN" -> R.drawable.ic_wx_rainy
        "THUNDERSTORM" -> R.drawable.ic_wx_thunderstorm
        "SNOW" -> R.drawable.ic_wx_weather_snowy
        "FOG" -> R.drawable.ic_wx_foggy
        "WIND" -> R.drawable.ic_wx_air
        else -> R.drawable.ic_wx_cloud
    }

    /** Words for a condition, for screen readers next to an icon. */
    fun words(condition: String?): String = when (condition) {
        "CLEAR" -> "clear"
        "PARTLY_CLOUDY" -> "partly cloudy"
        "CLOUDY" -> "cloudy"
        "RAIN" -> "rain"
        "THUNDERSTORM" -> "thunderstorms"
        "SNOW" -> "snow or ice"
        "FOG" -> "fog"
        "WIND" -> "windy"
        else -> "weather"
    }

    /** Warm for highs: Tokyo Night orange in dark mode. 7:1 or better on backgrounds and cards. */
    @Composable
    fun highColor(): Color = if (isDark()) Color(0xFFFF9E64) else Color(0xFF8F3A00)

    /** Cool for lows: Tokyo Night cyan in dark mode. 7:1 or better on backgrounds and cards. */
    @Composable
    fun lowColor(): Color = if (isDark()) Color(0xFF7DCFFF) else Color(0xFF00597A)

    @Composable
    private fun isDark(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
}
