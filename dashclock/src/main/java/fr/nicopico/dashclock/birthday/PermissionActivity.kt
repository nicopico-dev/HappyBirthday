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

package fr.nicopico.dashclock.birthday

import android.Manifest.permission.READ_CONTACTS
import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import fr.nicopico.happybirthday.extensions.show
import fr.nicopico.happybirthday.extensions.toast
import kotlinx.android.synthetic.main.activity_permission.*


@TargetApi(Build.VERSION_CODES.M)
class PermissionActivity : Activity() {

    companion object {
        private val REQUEST_READ_CONTACTS = 1
        private val REQUEST_PERMISSION_SETTING = 2

        @JvmStatic
        fun createIntent(context: Context) = Intent(context, PermissionActivity::class.java)
    }

    private var neverAskAgain = false
    private var requestTimestamp = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        if (savedInstanceState == null) {
            if (checkSelfPermission(READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions()
            }
            else {
                finish()
            }
        }

        btnGrantPermission.setOnClickListener {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
            arrayOf(txtExplanation, btnGrantPermission).forEach(TextView::show)
            // Check if the permission was refused automatically (no clean way to do this :/)
            neverAskAgain = System.currentTimeMillis() - requestTimestamp < 200
        }
        else {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            neverAskAgain = false
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        if (neverAskAgain) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }

            try {
                startActivityForResult(intent, REQUEST_PERMISSION_SETTING)
            }
            catch(e: ActivityNotFoundException) {
                toast(R.string.permission_error_unable_to_grant)
            }
        }
        else {
            requestTimestamp = System.currentTimeMillis()
            requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
    }
}