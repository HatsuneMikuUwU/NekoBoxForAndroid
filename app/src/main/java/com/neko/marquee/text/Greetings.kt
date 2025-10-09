package com.neko.marquee.text

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import java.util.Calendar

/**
 * A custom TextView that displays a greeting message based on the time of day
 * and the device's language setting.
 */
class Greetings : AppCompatTextView {

    constructor(context: Context) : super(context) {
        updateGreeting()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        updateGreeting()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        updateGreeting()
    }

    override fun isFocused(): Boolean {
        return true // Ensures marquee effect works by making the TextView always appear focused
    }

    /**
     * Updates the text with a greeting message based on the current time and language.
     */
    private fun updateGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val language = resources.configuration.locales[0].language

        val greeting = when (language) {
            "in" -> getIndonesianGreeting(hour)
            "zh", "CN" -> getChineseGreeting(hour)
            else -> getEnglishGreeting(hour)
        }
        text = greeting
    }

    /** Helper methods to get greetings in different languages **/
    private fun getEnglishGreeting(hour: Int) = when (hour) {
        in 4..8 -> "Good Morning..."
        in 9..15 -> "Good Afternoon..."
        in 16..20 -> "Good Evening..."
        in 21..23 -> "Good Night..."
        else -> "It's time to go to sleep..."
    }

    private fun getIndonesianGreeting(hour: Int) = when (hour) {
        in 4..8 -> "Selamat Pagi..."
        in 9..15 -> "Selamat Siang..."
        in 16..20 -> "Selamat Sore..."
        in 21..23 -> "Selamat Malam..."
        else -> "Waktunya Tidur..."
    }

    private fun getChineseGreeting(hour: Int) = when (hour) {
        in 4..8 -> "早安..."
        in 9..15 -> "午安..."
        in 16..20 -> "暮安..."
        in 21..23 -> "晚安..."
        else -> "该去睡觉啦 ..."
    }
}
