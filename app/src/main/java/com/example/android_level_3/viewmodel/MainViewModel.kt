package com.example.android_level_3.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android_level_3.model.ContactListGenerator
import com.example.android_level_3.model.Contact

// TODO
//  1. перенести методы добавления/удаления из адаптера
//  2. убарать работу с таймером (его не нужно было реализовывать в задании)
//  3. разобраться со списком, сделать его типа List .

open class MainViewModel: ViewModel() {

    private var contactList = ContactListGenerator().getContactList()

    val onTickTimerMessage = MutableLiveData<Int>()
    val onFinishTimer = MutableLiveData<Boolean>()
    private var timer: CountDownTimer? = null

    var temporaryUserData: Contact? = null
    var temporaryUserPosition: Int = 0

    fun getContactList() = contactList

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