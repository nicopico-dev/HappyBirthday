package fr.nicopico.happybirthday.domain.model

class Birthday(
        val day: Int,
        val month: Int,
        val year: Int?
) : Comparable<Birthday> {
    init {
        if (day < 1 || day > 31) {
            throw IllegalArgumentException("day should be between 1 and 31 ($this)")
        }
        if (month < 1 && month > 12) {
            throw IllegalArgumentException("month should be between 1 and 12 ($this)")
        }
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