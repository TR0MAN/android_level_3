package com.example.android_level_3

import androidx.recyclerview.widget.DiffUtil

class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
    override fun areItemsTheSame(oldItem: Contact, newItem: Contact) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Contact, newItem: Contact) = oldItem == newItem
}