package com.example.android_level_3.adapter

import com.example.android_level_3.retrofit.model.Contact

interface ClickListener

interface ElementClickListener: ClickListener {
    fun onElementClickAction(contact: Contact)
}

interface ExtendedElementClickListener : ElementClickListener{
    fun onElementProfileClick(contact: Contact)

    fun onElementLongClick(contactId: Int)

    fun onElementChecked(checkBoxState: Boolean, contactId: Int)
}
