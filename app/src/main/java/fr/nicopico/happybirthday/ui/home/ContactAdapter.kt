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
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return inflater.inflate(R.layout.item_contact, parent, false).let(::ViewHolder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data!![position])
    }

    override fun getItemCount(): Int = data?.size ?: 0

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(contact: Contact) {
            itemView.imgAvatar.setImageURI(contact.avatarThumbnail)
            itemView.txtName.text = contact.displayName

            with(itemView.txtInfos) {
                val birthday = contact.birthday
                visibility = View.VISIBLE
                text = birthday.format("d MMMM,")

                if (contact.canComputeAge()) {
                    val nextBirthdayDate = birthday.nextBirthdayDate()
                    val age = contact.getAge(nextBirthdayDate)
                    if (age != null) {
                        append(" $age ans")
                    }
                }

                val nextOccurrence = birthday.inDays()
                append(" dans $nextOccurrence jours")
            }
        }
    }
}