package com.example.android_level_3

// TODO - возможно полностью избавиться от класса, добавить нужные константы в нужный класс
object Const {

    const val REQUEST_KEY = "fragmentResult"
    const val RESULT_KEY = "newUser"
    const val AVATAR_IMAGE_KEY = "avatarImage"
    const val REQUEST_GET_CONTACTS = "getUsers"
    const val REQUEST_DELETE_CONTACT = "deleteContact"
    const val REQUEST_RESTORE_CONTACT = "restoreContact"
    const val REQUEST_GROUP_DELETE = "deleteGroup"
    const val REQUEST_ADD_CONTACT = "addContact"
    const val REQUEST_GET_ALL_USERS = "getUsers"

    const val EMAIL = "email"
    const val PASSWORD = "password"

    const val DOT = '●'
    const val PREFERENCES_SETTINGS = "userMemory"
    const val PREFERENCES_EMAIL = "email"
    const val PREFERENCES_PASSWORD = "password"
    const val PREFERENCES_CHECKBOX = "checkBox_status"
    const val PREFERENCES_AUTOLOGIN = "auto_login"

    const val PREFERENCES_ACCESS_TOKEN = "authorisation_token"
    const val PREFERENCES_REFRESH_TOKEN = "refresh_token"
    const val PREFERENCES_USER_ID = "user_id"
    const val PREFERENCES_USER_NAME = "name"
    const val PREFERENCES_USER_CAREER = "career"
    const val PREFERENCES_USER_ADDRESS = "address"

    const val STATE_EMAIL_FIELD = "email_field"
    const val STATE_PASSWORD_FIELD = "password_field"
    const val STATE_CHECKBOX = "checkbox_status"

    const val RETROFIT_BASE_URL = "http://178.63.9.114:7777/api/"
    const val REQUEST_MAX_DELAY = 4000L
    const val REQUEST_DELAY = 100L


}

// FATAL EXCEPTION: main
// Process: com.example.android_level_3, PID: 7227
// java.io.EOFException: End of input at line 1 column 182661 path $.data
// at com.google.gson.stream.JsonReader.nextNonWhitespace(JsonReader.java:1401)
// at com.google.gson.stream.JsonReader.doPeek(JsonReader.java:482)
// at com.google.gson.stream.JsonReader.hasNext(JsonReader.java:414)