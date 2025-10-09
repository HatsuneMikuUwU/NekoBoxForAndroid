package com.neko.marquee.text

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import io.nekohasekai.sagernet.R
import java.util.Calendar

/**
 * A custom TextView that displays a greeting message based on the time of day.
 * It automatically uses the appropriate string resources for localization.
 */
class Greetings @JvmOverloads constructor(
    context: Context, 
    attrs: AttributeSet? = null, 
    defStyleAttr: Int = 0
): AppCompatTextView(context, attrs, defStyleAttr) {

    init {updateGreeting()
    }
    override fun onAttachedToWindow() {super.onAttachedToWindow()
        updateGreeting() }

    override fun isFocused(): Boolean {return true}

    /**
     * Updates the text with a greeting by selecting the correct string resource
     * based on the current time of day.
     */
    private fun updateGreeting() {val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        @StringRes val greetingResId = when (hour) {
            in 4..8 -> R.string.uwu_greeting_morning
            in 9..15 -> R.string.uwu_greeting_afternoon
            in 16..20 -> R.string.uwu_greeting_evening
            in 21..23 -> R.string.uwu_greeting_night
            else -> R.string.uwu_greeting_late_night
        }
        setText(greetingResId)
    }
}
