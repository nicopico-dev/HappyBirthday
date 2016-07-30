package fr.nicopico.happybirthday.ui.home

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import fr.nicopico.happybirthday.R
import fr.nicopico.happybirthday.data.repository.Repository
import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.inject.AppComponent
import fr.nicopico.happybirthday.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var contactRepository: Repository<Contact>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val contactAdapter = ContactAdapter(this)
        rvContacts.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = contactAdapter
        }

        contactRepository.list().toList().subscribe {
            contactAdapter.data = it
        }
    }

    override fun inject(component: AppComponent) {
        component.inject(this)
    }
}
