package fr.nicopico.happybirthday.extensions

import fr.nicopico.happybirthday.domain.model.Birthday
import org.threeten.bp.LocalDate


fun LocalDate.toBirthday() = Birthday(
        year = year,
        month = monthValue,
        day = dayOfMonth
)

fun today(): LocalDate = LocalDate.now()

fun date(year: Int, month: Int, day: Int): LocalDate = LocalDate.of(year, month, day)