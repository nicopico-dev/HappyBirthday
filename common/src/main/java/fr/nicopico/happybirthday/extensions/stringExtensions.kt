package fr.nicopico.happybirthday.extensions

import android.net.Uri

fun String.asUri(): Uri = Uri.parse(this)