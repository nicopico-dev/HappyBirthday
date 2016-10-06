package fr.nicopico.happybirthday.extensions

import org.junit.Test
import org.threeten.bp.LocalDate

class DateExtensionsTest {

    @Test
    fun toBirthday() {
        val b = LocalDate.of(1980, 6, 10).toBirthday()
        assert(b.year == 1980
                && b.month == 6
                && b.day == 10)
    }

}