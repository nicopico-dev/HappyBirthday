package fr.nicopico.happybirthday.extensions

import android.database.Cursor

fun Cursor.intValue(col: String): Int? = getInt(getColumnIndexOrThrow(col))
fun Cursor.longValue(col: String): Long? = getLong(getColumnIndexOrThrow(col))
fun Cursor.floatValue(col: String): Float? = getFloat(getColumnIndexOrThrow(col))
fun Cursor.doubleValue(col: String): Double? = getDouble(getColumnIndexOrThrow(col))
fun Cursor.stringValue(col: String): String? = getString(getColumnIndexOrThrow(col))
