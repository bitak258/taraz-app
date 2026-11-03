package com.example.util

import androidx.compose.runtime.Composable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object JalaliDateFormatter {

    /**
     * Formats a timestamp (millis) to a Persian Jalali date string (e.g. "۱۲ آبان ۱۴۰۵")
     */
    fun format(millis: Long): String {
        val jDate = Fa.millisToJalali(millis)
        return jDate.format()
    }

    /**
     * Formats a timestamp to Jalali numeric string (e.g. "1405/08/12")
     */
    fun formatNumeric(millis: Long): String {
        val jDate = Fa.millisToJalali(millis)
        val y = jDate.year.toString()
        val m = jDate.month.toString().padStart(2, '0')
        val d = jDate.day.toString().padStart(2, '0')
        return Fa.digits("$y/$m/$d")
    }
}
