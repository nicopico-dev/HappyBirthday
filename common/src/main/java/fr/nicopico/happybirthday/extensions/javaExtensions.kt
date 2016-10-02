package fr.nicopico.happybirthday.extensions

import fr.nicopico.happybirthday.domain.model.Birthday
import java.util.*
import java.util.concurrent.TimeUnit


fun Calendar.toBirthday() = Birthday(
        year = get(Calendar.YEAR),
        month = get(Calendar.MONTH) + 1,
        day = get(Calendar.DAY_OF_MONTH)
)

fun Birthday.toCalendar() = calendar(year = year!!, month = month, day = day)

fun calendar(year: Int, month: Int, day: Int): Calendar {
    return Calendar.getInstance().apply {
        clear()
        set(year, month - 1, day)
    }
}

private val DELTA = TimeUnit.MINUTES.toMillis(5)
private var todayCache: Calendar? = null

fun today(): Calendar {
    var today = todayCache
    if (today == null
            || System.currentTimeMillis() - today.timeInMillis > DELTA) {
        today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    return today!!
}
