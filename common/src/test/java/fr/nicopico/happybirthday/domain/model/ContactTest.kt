package fr.nicopico.happybirthday.domain.model

import fr.nicopico.happybirthday.extensions.date
import org.junit.Test
import org.threeten.bp.LocalDate
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContactTest {

    @Test
    fun testCanComputeAge() {
        val b = Birthday(1980, 6, 10)
        val c = Contact(1, "1", "Nicolas P.", b)

        assertTrue { c.canComputeAge() }
    }

    @Test
    fun testCannotComputeAge() {
        val b = Birthday(6, 10)
        val c = Contact(1, "1", "Nicolas P.", b)

        assertFalse { c.canComputeAge() }
    }

    @Test
    fun testAge() {
        val b = Birthday(1980, 6, 10)
        val c = Contact(1, "1", "Nicolas P.", b)
        val reference = date(2016, 10, 2)
        assert(c.getAge(reference) == 36)
    }

    @Test
    fun testNoYear() {
        val b = Birthday(6, 10)
        val c = Contact(1, "1", "Nicolas P.", b)
        val reference = date(2016, 10, 2)
        assert(c.getAge(reference) == null)
    }

    @Test
    fun testAgeNextYear() {
        val b = Birthday(1980, 6, 10)
        val c = Contact(1, "1", "Nicolas P.", b)
        val reference = date(2017, 1, 2)
        assert(c.getAge(reference) == 36)
    }

    @Test
    fun testAgeOnBirthday() {
        val b = Birthday(1980, 6, 10)
        val c = Contact(1, "1", "Nicolas P.", b)
        val reference = date(2016, 6, 10)
        assert(c.getAge(reference) == 36)
    }

    @Test
    fun testNextBirthdaySorter() {
        val rnd = Random()
        val creator = { name: String, day: Int, month: Int, year: Int? ->
            Contact(
                    rnd.nextLong(),
                    name,
                    name,
                    Birthday(year, month, day)
            )
        }

        val expected = listOf(
                creator("Emilie", 7, 10, 1978),
                creator("Melanie", 7, 10, null),
                creator("Clément", 10, 10, null),
                creator("André", 27, 10, 1951),
                creator("Hervé", 2, 12, null),
                creator("Cécile", 2, 1, 1986),
                creator("Elise", 10, 6, 1980),
                creator("Nicolas", 10, 6, 1980),
                creator("Mireille", 17, 6, 1950)
        )

        val shuffled = expected.toMutableList().apply {
            Collections.shuffle(this)
        }

        val sorter = nextBirthdaySorter(LocalDate.of(2016, 10, 7))
        val sorted = shuffled.sortedWith(Comparator<Contact> { o1, o2 -> sorter.invoke(o1, o2) })

        assert(sorted == expected, { "\nexpect: $expected\nactual: $sorted" })
    }
}