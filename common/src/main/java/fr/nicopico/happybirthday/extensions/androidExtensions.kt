package fr.nicopico.happybirthday.extensions

import android.content.Context
import android.database.Cursor
import com.tbruyelle.rxpermissions.RxPermissions
import rx.Observable

fun Cursor.intValue(col: String): Int? = getInt(getColumnIndexOrThrow(col))
fun Cursor.longValue(col: String): Long? = getLong(getColumnIndexOrThrow(col))
fun Cursor.floatValue(col: String): Float? = getFloat(getColumnIndexOrThrow(col))
fun Cursor.doubleValue(col: String): Double? = getDouble(getColumnIndexOrThrow(col))
fun Cursor.stringValue(col: String): String? = getString(getColumnIndexOrThrow(col))

inline fun <T> Context.ensurePermission(vararg permissions: String, crossinline action: () -> Observable<T>): Observable<T> {
    return RxPermissions
            .getInstance(this)
            .request(*permissions)
            .switchMap { granted ->
                when {
                    granted -> action()
                    else -> Observable.empty()
                }
            }
}
