package com.example.android_level_3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android_level_3.model.ContactListGenerator
import com.example.android_level_3.model.Contact


open class MainViewModel: ViewModel() {

    private val contactList = MutableLiveData<List<Contact>>()
    val observableContactList: LiveData<List<Contact>> = contactList

    private var deletedContactData: Contact? = null
    private var deletedContactPosition: Int = 0

    init {
        contactList.value = ContactListGenerator().createContactList()
    }

    fun addContact(newContact: Contact) {
        if (contactList.value?.size != 0) {
            var position = contactList.value?.last()?.id
            if (position == null) {
                position = 0
            }
            val contact = newContact.copy(id = position + 1)
            contactList.value = contactList.value?.toMutableList()?.apply {
                add(contact)
            }
        } else {
            contactList.value = contactList.value?.toMutableList()?.apply {
                val contact = newContact.copy(id = 1)
                add(0, contact)
            }
        }
    }

    fun deleteContact(contactForDelete: Contact) {
        if (contactList.value?.contains(contactForDelete) == false) return

        deletedContactPosition = contactList.value?.indexOf(contactForDelete)!!
        deletedContactData = contactForDelete

        contactList.value = contactList.value?.toMutableList()?.apply {
            remove(contactForDelete)
        }
    }

    fun restoreContact() {
        if (contactList.value?.contains(deletedContactData) == true) return

        deletedContactData?.let {
            contactList.value = contactList.value?.toMutableList()?.apply {
                add(deletedContactPosition, it)
            }
        }
    }

    fun deleteMultipleContact() {

    }


}