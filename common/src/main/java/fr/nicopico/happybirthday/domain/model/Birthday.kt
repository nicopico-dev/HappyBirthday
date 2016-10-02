package fr.nicopico.happybirthday.domain.model

import fr.nicopico.happybirthday.extensions.today
import java.util.*

class Birthday(
        val year: Int?,
        val month: Int,
        val day: Int
) : Comparable<Birthday> {

    companion object {
        private val regex = "%t".toRegex()
    }

    constructor(month: Int, day: Int) : this(null, month, day)

    @Suppress("DEPRECATION")
    private val date by lazy {
        Date((year ?: 1900) - 1900, month - 1, day)
    }

    private val timestamp by lazy {
        date.time
    }

    init {
        if (day < 1 || day > 31) {
            throw IllegalArgumentException("day should be between 1 and 31 ($this)")
        }
        if (month < 1 || month > 12) {
            throw IllegalArgumentException("month should be between 1 and 12 ($this)")
        }
    }

    fun format(format: String): String {
        val count = regex.findAll(format).let { it.count() }
        val args = kotlin.arrayOfNulls<Date>(count).apply {
            for (i in 0..count - 1) {
                this[i] = date
            }
        }
        return String.format(Locale.getDefault(), format, *args)
    }

    fun withYear(pYear: Int) = Birthday(day = day, month = month, year = pYear)

    fun inDays(reference: Calendar = today()): Int {
        val yearBirthday = withYear(reference.get(Calendar.YEAR))

        val referenceTime: Long = reference.timeInMillis
        val birthdayTime: Long = when (referenceTime <= yearBirthday.timestamp) {
            true -> yearBirthday.timestamp
            false -> yearBirthday.withYear(yearBirthday.year!! + 1).timestamp
        }

        val delta: Long = birthdayTime - referenceTime
        return Math.ceil(delta / (1000.0 * 60 * 60 * 24)).toInt()
    }

    override fun toString(): String {
        return "$day/$month/${year ?: '?'}"
    }

    override fun compareTo(other: Birthday): Int {
        val dayCompare = day.compareTo(other.day)
        val monthCompare = month.compareTo(other.month)
        return when (monthCompare) {
            0 -> dayCompare
            else -> monthCompare
        }
    }
}