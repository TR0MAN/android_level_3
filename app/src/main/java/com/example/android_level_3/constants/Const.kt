package com.example.android_level_3.constants

object Const {

    const val RESULT_KEY = "newUser"
    const val AVATAR_IMAGE_KEY = "avatarImage"
    const val EMAIL = "email"
    const val PASSWORD = "password"
    const val DOT = '●'

    const val STATE_EMAIL_FIELD = "email_field"
    const val STATE_PASSWORD_FIELD = "password_field"
    const val STATE_CHECKBOX = "checkbox_status"
    const val SNACKBAR_DURATION = 5000

}

// FATAL EXCEPTION: main
// Process: com.example.android_level_3, PID: 7227
// java.io.EOFException: End of input at line 1 column 182661 path $.data
// at com.google.gson.stream.JsonReader.nextNonWhitespace(JsonReader.java:1401)
// at com.google.gson.stream.JsonReader.doPeek(JsonReader.java:482)
// at com.google.gson.stream.JsonReader.hasNext(JsonReader.java:414)