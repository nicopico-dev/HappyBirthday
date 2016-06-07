package fr.nicopico.happybirthday.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import fr.nicopico.happybirthday.App
import fr.nicopico.happybirthday.inject.AppComponent

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as? App)?.let {
            inject(it.component)
        }
    }

    abstract fun inject(component: AppComponent)
}