package com.js.floatinglooger

import android.content.Context
import android.widget.Toast

class Test {

    fun showToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}