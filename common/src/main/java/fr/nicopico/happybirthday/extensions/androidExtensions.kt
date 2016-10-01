package fr.nicopico.happybirthday.extensions

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.widget.Toast
import com.tbruyelle.rxpermissions.RxPermissions
import rx.Observable

fun String.asUri(): Uri = Uri.parse(this)

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

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}