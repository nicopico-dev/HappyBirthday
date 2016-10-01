package fr.nicopico.happybirthday.domain.model

import fr.nicopico.happybirthday.extensions.calendar
import org.junit.Test
import java.util.*
import kotlin.test.*


class BirthdayTest {

    @Test
    fun invalidMonth0() {
        assertFailsWith(IllegalArgumentException::class) {
            Birthday(0, 1)
        }
    }

    @Test
    fun invalidMonth13() {
        assertFailsWith(IllegalArgumentException::class) {
            Birthday(13, 1)
        }
    }

    @Test
    fun invalidDay0() {
        assertFailsWith(IllegalArgumentException::class) {
            Birthday(1, 0)
        }
    }

    @Test
    fun invalidDay32() {
        assertFailsWith(IllegalArgumentException::class) {
            Birthday(1, 32)
        }
    }

    @Test
    fun withYear() {
        val b = Birthday(6, 20)
        val withYear = b.withYear(2006)
        assert(
                withYear.day == b.day
                        && withYear.month == b.month
                        && withYear.year == 2006
        )
    }

    @Test
    fun leapYear() {
        TODO("Check birthday on leap year")
    }

    @Test
    fun format() {
        val b = Birthday(1, 20)
        Locale.setDefault(Locale.US)
        val format = b.format("%te %tB")
        assert(format.toUpperCase() == "20 JANUARY")
    }

    @Test
    fun inDaysBefore() {
        val b = Birthday(1980, 6, 10)
        val reference = calendar(2016, 6, 1)
        val expected = 9
        val actual = b.inDays(reference)
        assert(actual == expected, { "Computed $actual instead of $expected from ${reference.time} to $b" })
    }

    @Test
    fun inDaysSame() {
        val b = Birthday(1980, 6, 10)
        val reference = calendar(2016, 6, 10)
        val expected = 0
        val actual = b.inDays(reference)
        assert(actual == expected, { "Computed $actual instead of $expected from ${reference.time} to $b" })
    }

    @Test
    fun inDaysAfter() {
        val b = Birthday(1984, 1, 2)
        val reference = calendar(2016, 12, 31)
        val expected = 2
        val actual = b.inDays(reference)
        assert(actual == expected, { "Computed $actual instead of $expected from ${reference.time} to $b" })
    }
}