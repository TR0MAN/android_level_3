package com.example.android_level_3.old_classes

import java.io.Serializable

// TODO - добавить ID и возможно поле isChecked
data class TestContact (
    val id: Int,
    val contactName: String,
    val contactCareer: String,
    val contactEmail: String = "default.mail@mail.com",
    val contactPhoneNumber: String = "+38-012-345-67-89",
    val contactAddress: String = "Lake Village, Sweet Home, 124",
    val contactBirthday: String = "01/01/2023",
    val contactImage: String,
    var isSelected: Boolean = false
) : Serializable
