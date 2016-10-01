package fr.nicopico.happybirthday.extensions

import org.junit.Test

import java.util.Calendar.*
import java.util.*

class JavaExtensionsKtTest {

    @Test
    fun testCalendar() {
        val c1 = calendar(1980, 6, 10)
        assert(c1.get(YEAR) == 1980
                && c1.get(MONTH) == 5
                && c1.get(DAY_OF_MONTH) == 10)
    }

    @Test
    fun toBirthday() {
        val b = calendar(1980, 6, 10).toBirthday()
        assert(b.year == 1980
                && b.month == 6
                && b.day == 10)
    }

    @Test
    fun testToday() {
        val today = today()
        val fields = arrayOf(HOUR, MINUTE, SECOND, MILLISECOND)
        fields.map { today.get(it) }
                .all { it == 0 }
    }

    @Test
    fun testTodaySecondCall() {
        val today1 = today()
        val today2 = today()

        val fields = arrayOf(YEAR, MONTH, Calendar.DAY_OF_MONTH, HOUR, MINUTE, SECOND, MILLISECOND)
        fields.all { today1.get(it) == today2.get(it) }
    }

}