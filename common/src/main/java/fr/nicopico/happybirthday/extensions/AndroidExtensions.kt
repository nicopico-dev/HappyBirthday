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

// FIXME inline does not work with proguard
fun <T> Context.ensurePermission(vararg permissions: String, action: () -> Observable<T>): Observable<T> {
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