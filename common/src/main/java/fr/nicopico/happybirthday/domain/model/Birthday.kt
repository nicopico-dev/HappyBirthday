package fr.nicopico.happybirthday.domain.model

import java.util.*

class Birthday(
        val day: Int,
        val month: Int,
        val year: Int?
) : Comparable<Birthday> {

    companion object {
        private val regex = "%t".toRegex()
    }

    @Suppress("DEPRECATION")
    private val date by lazy {
        Date(year ?: 1970, month, day)
    }

    init {
        if (day < 1 || day > 31) {
            throw IllegalArgumentException("day should be between 1 and 31 ($this)")
        }
        if (month < 1 && month > 12) {
            throw IllegalArgumentException("month should be between 1 and 12 ($this)")
        }
    }

    fun format(format: String): String {
        val count = regex.findAll(format).let { it.count() }
        val args = kotlin.arrayOfNulls<Date>(count).apply {
            for (i in 0..count-1) {
                this[i] = date
            }
        }
        return String.format(Locale.getDefault(), format, *args)
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