package com.example.android_level_3.viewmodel

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android_level_3.model.ContactListGenerator
import com.example.android_level_3.model.Contact

// TODO
//  1. перенести методы добавления/удаления из адаптера                                             - ГОТОВО
//  2. убарать работу с таймером (его не нужно было реализовывать в задании)
//  3. разобраться со списком, сделать его типа List.                                               - ГОТОВО

open class MainViewModel: ViewModel() {

    private val contactList = MutableLiveData<List<Contact>>()
    val observableContactList: LiveData<List<Contact>> = contactList

    private var deletedContactData: Contact? = null
    private var deletedContactPosition: Int = 0
    private var timer: CountDownTimer? = null

    val onTickTimerMessage = MutableLiveData<Int>()
    val onFinishTimer = MutableLiveData<Boolean>()

    init {
        contactList.value = ContactListGenerator().createContactList()
    }

//    fun getContactList() = contactList                                          // TODO DELETE ???

    fun addContact(newContact: Contact) {

        var position = contactList.value?.last()?.id
        if (position == null) {
            position = 0
        }
        val contact = newContact.copy(id = position + 1)
//        Log.d("TAG", "[ViewModel] > UPDATED contact = $contact")

        contactList.value = contactList.value?.toMutableList()?.apply {
            add(contact)
        }
    }

    fun deleteContact(contactForDelete: Contact) {
        if (contactList.value?.contains(contactForDelete) == false) return

        deletedContactPosition = contactList.value?.indexOf(contactForDelete)!!
//        Log.d("TAG", "[ViewModel] > Position = $deletedContactPosition")
        deletedContactData = contactForDelete
//        Log.d("TAG", "[ViewModel] > Data = $deletedContactData")

        contactList.value = contactList.value?.toMutableList()?.apply {
            remove(contactForDelete)

        }
    }

    fun restoreContact() {
        if (contactList.value?.size == 0
            || contactList.value?.contains(deletedContactData) == true) return

        deletedContactData?.let {
            contactList.value = contactList.value?.toMutableList()?.apply {
                add(deletedContactPosition, it)
            }
        }

    }

    fun deleteMultipleContact() {

    }

    // отсчет 5 секунд для возможности восстановления контакта
    fun timerStart() {
        onFinishTimer.value = false
        if (timer == null) {
            timer = object : CountDownTimer(5100L, 1000L) {

                override fun onTick(millisUntilFinished: Long) {
                    onTickTimerMessage.value = (millisUntilFinished / 1000L).toInt()
                }

                override fun onFinish() {
                    onFinishTimer.value = true
                    timer = null
                }
            }.start()
        }
    }

    // остановка таймера, если контакт был восстановлен
    fun timerStop(){
        timer?.cancel()
        timer = null
    }

}










//        val newList = mutableListOf<Contact>()
//        newList.add(
//            Contact(
//                id = 30,
//                contactName = "Alla",
//                contactCareer = "Actor",
//                contactEmail = "sdsd@mail.com",
//                contactPhoneNumber = "112233444",
//                contactAddress = "Lviv",
//                contactBirthday = "01.01.01",
//                contactImage = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8cGVyc29ufGVufDB8fDB8fHww"
//            )
//        )
//
//        newList.add(
//            Contact(
//                id = 31,
//                contactName = "Ben",
//                contactCareer = "Actor",
//                contactEmail = "sdsd@mail.com",
//                contactPhoneNumber = "112233444",
//                contactAddress = "Lviv",
//                contactBirthday = "01.01.01",
//                contactImage = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8cGVyc29ufGVufDB8fDB8fHww"
//            )
//        )
//
//        newList.add(
//            Contact(
//                id = 32,
//                contactName = "Camilla",
//                contactCareer = "Actor",
//                contactEmail = "sdsd@mail.com",
//                contactPhoneNumber = "112233444",
//                contactAddress = "Lviv",
//                contactBirthday = "01.01.01",
//                contactImage = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8cGVyc29ufGVufDB8fDB8fHww"
//            )
//        )
//        observableContactList.value = newList