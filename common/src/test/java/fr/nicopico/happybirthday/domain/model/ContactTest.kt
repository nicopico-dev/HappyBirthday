package fr.nicopico.happybirthday.domain.model

import fr.nicopico.happybirthday.extensions.date
import org.junit.Test

class ContactTest {

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
}