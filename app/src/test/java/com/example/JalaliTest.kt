package com.example

import com.example.util.Fa
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class JalaliTest {

    @Test
    fun testPersianDigits() {
        assertEquals("۱۲۳۴۵۶۷۸۹۰", Fa.digits("1234567890"))
        assertEquals("۱۴۰۵/۰۸/۱۲", Fa.digits("1405/08/12"))
    }

    @Test
    fun testCurrencyFormat() {
        assertEquals("۱۵۰,۰۰۰ تومان", Fa.money(150000L))
    }

    @Test
    fun testCurrentJalaliDate() {
        val jDate = Fa.millisToJalali(System.currentTimeMillis())
        assertNotNull(jDate)
        assertEquals(true, jDate.year > 1400)
    }
}
