package com.example.android_level_3.authorization

import java.util.Calendar
import java.util.Date

object Helper {

    fun dateValidator(date: String): String? {

        // checking for wrong DATE format
        if (!date.matches("^[0-9]{4,5}\\/[0-9]{2,3}\\/[0-9]{2,3}\$".toRegex())) {
            return "( wrong format, for example 2000/05/25 )"
        }

        val dateList = date.split('/')
        val enteredDate = Calendar.getInstance().apply {
            set(dateList[0].toInt(), dateList[1].toInt().minus(1), dateList[2].toInt())
        }
        val currentDate = Calendar.getInstance()

        // checking for wrong YEAR parameter
        if (dateList[0].toInt() < 1 || dateList[0].toInt() > currentDate.get((Calendar.YEAR))) {
            return "( wrong YEAR parameter )"
        }
        // checking for wrong MONTH parameter
        if (dateList[1].toInt() > 12 || dateList[1].toInt() < 1) {
            return "( wrong MONTH parameter )"
        }
        // checking for wrong DAY parameter
        if (dateList[2].toInt() > 31 || dateList[2].toInt() < 1) {
            return "( wrong DAY parameter )"
        }
        // checking for a future date
        if (enteredDate > currentDate) {
            return "( wrong, this date has not yet arrived )"
        }

        return null
    }

    fun dataFieldValidator(string: String): String? {
        if (string.isEmpty() || string.isBlank()) return null
        return string
    }

    fun getBirthday(birthdayDate: String): Date {
        val dateList = birthdayDate.split('/')
        val calendar = Calendar.getInstance(). apply {
            set(dateList[0].toInt(), dateList[1].toInt().minus(1), dateList[2].toInt(), 0,0, 0)
        }
        return calendar.time
    }

}