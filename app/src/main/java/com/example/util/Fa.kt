package com.example.util

import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object Fa {
    val iranTimeZone: TimeZone = TimeZone.getTimeZone("Asia/Tehran")

    fun digits(s: String): String {
        return s.map { if (it in '0'..'9') '۰' + (it - '0') else it }.joinToString("")
    }

    fun money(v: Long): String {
        val formatted = NumberFormat.getIntegerInstance(Locale.US).format(v)
        return digits(formatted) + " تومان"
    }

    fun number(v: Number): String {
        val formatted = NumberFormat.getIntegerInstance(Locale.US).format(v)
        return digits(formatted)
    }

    data class JalaliDate(val year: Int, val month: Int, val day: Int) {
        fun format(): String {
            val monthNames = arrayOf(
                "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
                "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
            )
            val mName = if (month in 1..12) monthNames[month - 1] else ""
            return "${digits(day.toString())} $mName ${digits(year.toString())}"
        }
    }

    fun millisToJalali(millis: Long): JalaliDate {
        val cal = Calendar.getInstance(iranTimeZone).apply { timeInMillis = millis }
        val gY = cal.get(Calendar.YEAR)
        val gM = cal.get(Calendar.MONTH) + 1
        val gD = cal.get(Calendar.DAY_OF_MONTH)
        return gregorianToJalali(gY, gM, gD)
    }

    fun jalaliToMillis(year: Int, month: Int, day: Int): Long {
        val (gY, gM, gD) = jalaliToGregorian(year, month, day)
        val cal = Calendar.getInstance(iranTimeZone).apply {
            set(gY, gM - 1, gD, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getTodayMidnightMillis(): Long {
        val cal = Calendar.getInstance(iranTimeZone).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy_ = gy - 1600
        var gm_ = gm - 1
        var gd_ = gd - 1

        var gDayNo = 365 * gy_ + (gy_ + 3) / 4 - (gy_ + 99) / 100 + (gy_ + 399) / 400
        for (i in 0 until gm_) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm_ > 1 && ((gy_ % 4 == 0 && gy_ % 100 != 0) || (gy_ % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd_

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053
        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 365) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        var jd = 0
        for (i in 0..11) {
            val days = jDaysInMonth[i]
            if (jDayNo < days) {
                jm = i + 1
                jd = jDayNo + 1
                break
            }
            jDayNo -= days
        }
        return JalaliDate(jy, jm, jd)
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
        var jy_ = jy - 979
        var jm_ = jm - 1
        var jd_ = jd - 1

        var jDayNo = 365 * jy_ + (jy_ / 33) * 8 + (jy_ % 33 + 3) / 4 + 78 + jd_
        for (i in 0 until jm_) {
            jDayNo += jDaysInMonth[i]
        }

        var gDayNo = jDayNo + 79
        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524
            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }

        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 365) {
            gy += (gDayNo - 1) / 365
            gDayNo = (gDayNo - 1) % 365
            leap = false
        }

        var gd = gDayNo + 1
        val gDaysInMonth = intArrayOf(0, 31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        for (i in 1..12) {
            val v = gDaysInMonth[i]
            if (gd <= v) {
                gm = i
                break
            }
            gd -= v
        }
        return Triple(gy, gm, gd)
    }
}
