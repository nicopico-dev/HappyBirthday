/*
 * Copyright 2016 Nicolas Picon <nicopico.dev@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.nicopico.happybirthday.domain.model

import android.util.LruCache
import fr.nicopico.happybirthday.extensions.today
import org.threeten.bp.LocalDate
import org.threeten.bp.MonthDay
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

data class Birthday(
        val year: Int?,
        val month: Int,
        val day: Int
) : Comparable<Birthday> {

    companion object {
        private val formatterCache = object : LruCache<String, DateTimeFormatter>(5) {
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
            withYear(1904).toLocalDate()
        }
    }

    /**
     * Format this birthday date as a String
     * @param format defined in [DateTimeFormatter]
     */
    fun format(format: String): String {
        return formatterCache[format].format(localDate)
    }

    /**
     * Create a copy of this birthday, with the year set as [pYear]
     */
    fun withYear(pYear: Int) = when (pYear) {
        year -> this
        else -> Birthday(day = day, month = month, year = pYear)
    }

    /**
     * Return the number of days between [reference] and the next birthday
     * @param reference Reference date, default to today
     */
    fun inDays(reference: LocalDate = today()): Long {
        return ChronoUnit.DAYS.between(reference, nextBirthdayDate(reference))
    }

    /**
     * Return the next birthday day, starting from [reference]
     * @param reference Reference date, default to today
     */
    fun nextBirthdayDate(reference: LocalDate = today()): LocalDate {
        val yearLocalDate = withYear(reference.year).toLocalDate()
        return when (reference <= yearLocalDate) {
            true -> yearLocalDate
            false -> yearLocalDate.plusYears(1)
        }
    }

    /**
     * Convert this birthday to a [LocalDate]
     * @throws UnsupportedOperationException if the birthday's [year] is not specified
     */
    fun toLocalDate(): LocalDate {
        if (year == null) {
            throw UnsupportedOperationException("Cannot convert this birthday $this to localDate (no year)")
        }
        return MonthDay.of(month, day).atYear(year)
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