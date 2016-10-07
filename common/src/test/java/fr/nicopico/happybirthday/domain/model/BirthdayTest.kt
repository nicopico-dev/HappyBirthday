package fr.nicopico.happybirthday.domain.model

import fr.nicopico.happybirthday.extensions.date
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
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
    fun marsBirthdayInDaysOnLeapYear() {
        val b = Birthday(2007, 3, 1)
        val reference = date(2016, 2, 28)
        assert(b.inDays(reference) == 2L)
    }

    @Test
    fun leapBirthdayInDaysOnNonLeapYear() {
        val b = Birthday(2008, 2, 29)
        val reference = date(2017, 2, 28)
        Locale.setDefault(Locale.US)
        assert(b.inDays(reference) == 0L)
    }

    @Test
    fun leapBirthdayInDaysOnNonLeapYearTaiwan() {
        val b = Birthday(2008, 2, 29)
        val reference = date(2017, 2, 28)
        Locale.setDefault(Locale.TAIWAN)
        assert(b.inDays(reference) == 1L)
    }

    @Test
    fun leapBirthdayToLocalDateOnNonLeapYear() {
        val b = Birthday(2008, 2, 29)
        b.withYear(2017).toLocalDate()
    }

    @Test
    fun leapBirthdayWithYearConservation() {
        val b = Birthday(2008, 2, 29)
        val b2 = b.withYear(2016).withYear(2008)
        assert(b == b2, { "$b is not equal to $b2" })
    }

    @Test
    fun format() {
        val b = Birthday(1944, 1, 20)
        Locale.setDefault(Locale.US)
        val format = b.format("d MMMM")
        assert(format.toUpperCase() == "20 JANUARY")
    }

    @Test
    fun formatNoYear() {
        val b = Birthday(1, 20)
        Locale.setDefault(Locale.US)
        val format = b.format("d MMMM")
        assert(format.toUpperCase() == "20 JANUARY")
    }

    @Test
    fun formatLeapBirthdayNoYear() {
        val b = Birthday(2, 29)
        Locale.setDefault(Locale.US)
        val format = b.format("d MMMM")
        assert(format.toUpperCase() == "29 FEBRUARY")
    }

    @Test
    fun inDaysBefore() {
        val b = Birthday(1980, 6, 10)
        val reference = date(2016, 6, 1)
        val expected = 9L
        val actual = b.inDays(reference)
        assert(actual == expected, { "Computed $actual instead of $expected from $reference to $b" })
    }

    @Test
    fun inDaysSame() {
        val b = Birthday(1980, 6, 10)
        val reference = date(2016, 6, 10)
        val expected = 0L
        val actual = b.inDays(reference)
        assert(actual == expected, { "Computed $actual instead of $expected from $reference to $b" })
    }

    @Test
    fun inDaysAfter() {
        val b = Birthday(1984, 1, 2)
        val reference = date(2016, 12, 31)
        val expected = 2L
        val actual = b.inDays(reference)
        assert(actual == expected, { "Computed $actual instead of $expected from $reference to $b" })
    }

    @Test
    fun toLocalDate() {
        val ld = Birthday(1980, 6, 10).toLocalDate()
        assert(ld.year == 1980
                && ld.monthValue == 6
                && ld.dayOfMonth == 10)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun toLocalDateNoYear() {
        Birthday(6, 10).toLocalDate()
    }
}