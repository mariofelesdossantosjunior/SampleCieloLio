package com.mario.samplecielo.util

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mario.samplecielo.R

fun AppCompatActivity.showCustomDialog(
    menssage: String,
    actionPositive: () -> Unit,
    actionNegative: () -> Unit
) {
    val dialog = AlertDialog.Builder(this)
        .setTitle(R.string.information)
        .setMessage(menssage)
        .setPositiveButton(R.string.actions_ok) { _, _ -> actionPositive() }
        .setNegativeButton(R.string.actions_cancel) { _, _ -> actionNegative() }
        .create()

    dialog.show()
}