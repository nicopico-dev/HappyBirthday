package fr.nicopico.happybirthday.ui

import android.os.Bundle
import fr.nicopico.happybirthday.R
import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.data.repository.Repository
import fr.nicopico.happybirthday.inject.AppComponent
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var contactRepository: Repository<Contact>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        assert(contactRepository != null)
    }

    override fun inject(component: AppComponent) {
        component.inject(this)
    }
}
