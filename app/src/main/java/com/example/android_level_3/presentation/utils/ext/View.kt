package com.example.android_level_3.presentation.utils.ext

import android.view.View

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.invisibleIf(state: Boolean) {
    visibility = if (state) View.VISIBLE else View.INVISIBLE
}