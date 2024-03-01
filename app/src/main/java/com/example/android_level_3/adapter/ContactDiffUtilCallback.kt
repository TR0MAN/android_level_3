package com.example.android_level_3.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.android_level_3.model.Contact

class ContactDiffUtilCallback : DiffUtil.ItemCallback<Contact>() {
    override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem == newItem
    }

}