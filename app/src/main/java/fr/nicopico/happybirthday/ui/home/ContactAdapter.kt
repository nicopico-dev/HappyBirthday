package fr.nicopico.happybirthday.ui.home

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fr.nicopico.happybirthday.R
import fr.nicopico.happybirthday.domain.model.Contact
import kotlinx.android.synthetic.main.item_contact.view.*

class ContactAdapter(context: Context) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    private val inflater: LayoutInflater

    init {
        inflater = LayoutInflater.from(context)
    }

    var data: List<Contact>? = null
        set(value) {
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return inflater.inflate(R.layout.item_contact, parent, false).let { ViewHolder(it) }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data!![position])
    }

    override fun getItemCount(): Int = data?.size ?: 0

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(contact: Contact) {
            itemView.imgAvatar.setImageURI(contact.avatarThumbnail)
            itemView.txtName.text = contact.displayName
            itemView.txtInfos.visibility = View.GONE
        }
    }
}