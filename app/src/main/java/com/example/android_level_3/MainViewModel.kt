package com.example.android_level_3

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _contactListLiveData = MutableLiveData<List<Contact>>(emptyList())
    internal val contactListLiveData: LiveData<List<Contact>> = _contactListLiveData

    val onTickTimerMessage = MutableLiveData<Int>()
    val onFinishTimer = MutableLiveData<Boolean>()
    private var timer: CountDownTimer? = null

    var temporaryContactData: Contact? = null
    var temporaryUserPosition: Int = 0

    init {
        _contactListLiveData.value = ContactListGenerator().getContactList()
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
    fun timerStop() {
        timer?.cancel()
        timer = null
    }

    fun removeContact(contact: Contact) {
        _contactListLiveData.value = _contactListLiveData.value?.toMutableList()?.apply {
            remove(contact)
        }
    }

    fun addContact(contact: Contact, index: Int = 0) {
        if (index < 0 || contactListLiveData.value?.contains(contact) == true) return
        _contactListLiveData.value = _contactListLiveData.value?.toMutableList()?.apply {
            add(index, contact)
        }
    }

}