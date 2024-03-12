package com.example.android_level_3.adapter

import com.example.android_level_3.model.Contact

interface ElementClickListener {

    fun onElementDeleteClick(contact: Contact)

    fun onElementProfileClick(contact: Contact)

    fun onElementLongClick(contactId: Int)

    fun onElementChecked(checkBoxState: Boolean, contactId: Int)
}