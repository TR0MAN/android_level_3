package com.example.android_level_3.presentation.ui.adapter

import com.example.android_level_3.data.retrofit.model.Contact

interface ClickListener

interface ElementClickListener: ClickListener {
    fun onElementClickAction(contact: Contact)
}

interface ExtendedElementClickListener : ElementClickListener {
    fun onElementProfileClick(contact: Contact)

    fun onElementLongClick(contactId: Int)

    fun onElementChecked(checkBoxState: Boolean, contactId: Int)
}
