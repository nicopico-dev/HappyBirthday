package fr.nicopico.happybirthday.extensions

import android.database.Cursor

fun Cursor.intValue(col: String) = getInt(getColumnIndexOrThrow(col))
fun Cursor.longValue(col: String) = getLong(getColumnIndexOrThrow(col))
fun Cursor.floatValue(col: String) = getFloat(getColumnIndexOrThrow(col))
fun Cursor.doubleValue(col: String) = getDouble(getColumnIndexOrThrow(col))
fun Cursor.stringValue(col: String): String = getString(getColumnIndexOrThrow(col))
