package com.example.android_level_3.viewmodel

import android.util.Log
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

    fun getContactList() = contactList.value?.toMutableList()

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

    fun deleteMultipleContact(listForDelete: List<Int>?) {
        if (listForDelete == null) return
        val listForRemove = mutableListOf<Contact>()

        listForDelete.forEach { contactId ->
            contactList.value?.filter { it.id == contactId }?.map {
                listForRemove.add(it)
            }
        }
        contactList.value = contactList.value?.toMutableList()?.apply {
            removeAll(listForRemove)
        }
    }

    // TODO - ВСЕ РАБОТАЕТ
    fun changeSelectableState3(contact: Contact) {
        contactList.value?.filter { it == contact }?.map {
            Log.d("TAG", "[VM][STATE] Filter [FOUND] - $it")
            val state = it.isSelected
            Log.d("TAG", "[VM][STATE] OLD Selected State $state")
            it.isSelected = !state
            Log.d("TAG", "[VM][STATE] NEW Selected State ${it.isSelected}")
            Log.d("TAG", "[VM][STATE] RESULT -> ID=${it.id} isSelect=${it.isSelected}")
        }
    }

    fun changeSelectableState(contactId: Int) {

        contactList.value?.filter { it.id == contactId }?.map {
            Log.d("TAG", "[VM][STATE] Filter [FOUND] - $it")
            val state = it.isSelected
            Log.d("TAG", "[VM][STATE] OLD Selected State $state")
            it.isSelected = !state
            Log.d("TAG", "[VM][STATE] NEW Selected State ${it.isSelected}")
            Log.d("TAG", "[VM][STATE] RESULT -> ID=${it.id} isSelect=${it.isSelected}")
        }
    }
}