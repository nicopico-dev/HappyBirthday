package fr.nicopico.happybirthday.domain.model

import android.util.LruCache
import fr.nicopico.happybirthday.extensions.today
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

data class Birthday(
        val year: Int?,
        val month: Int,
        val day: Int
) : Comparable<Birthday> {

    companion object {
        private val formatterCache = object: LruCache<String, DateTimeFormatter>(5) {
            override fun create(key: String): DateTimeFormatter {
                return DateTimeFormatter.ofPattern(key)
            }
        }
    }

    constructor(month: Int, day: Int) : this(null, month, day)

    init {
        if (day < 1 || day > 31) {
            throw IllegalArgumentException("day should be between 1 and 31 ($this)")
        }
        if (month < 1 || month > 12) {
            throw IllegalArgumentException("month should be between 1 and 12 ($this)")
        }
    }

    private val localDate by lazy {
        if (year != null) {
            toLocalDate()
        }
        else {
            withYear(1900).toLocalDate()
        }
    }

    fun format(format: String): String {
        return formatterCache[format].format(localDate)
    }

    fun withYear(pYear: Int) = Birthday(day = day, month = month, year = pYear)

    fun inDays(reference: LocalDate = today()): Long {
        val yearLocalDate = toLocalDate().withYear(reference.year)
        val nextBirthdayDate = when (reference <= yearLocalDate) {
            true -> yearLocalDate
            false -> yearLocalDate.plusYears(1)
        }

        return ChronoUnit.DAYS.between(reference, nextBirthdayDate)
    }

    /**
     * Convert this birthday to a [LocalDate]
     * @throws UnsupportedOperationException if the birthday's [year] is not specified
     */
    fun toLocalDate(): LocalDate {
        if (year == null) {
            throw UnsupportedOperationException("Cannot convert this birthday $this to localDate (no year)")
        }
        return LocalDate.of(year, month, day)
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