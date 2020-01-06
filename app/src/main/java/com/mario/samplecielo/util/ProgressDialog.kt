package com.mario.samplecielo.util

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask

class MyProgressDialog(context: Context) : AsyncTask<Void, Void, Void>() {

    private val progressDialog by lazy {
        ProgressDialog(context)
    }

    override fun onPreExecute() {
        progressDialog.setMessage("Processando...")
        progressDialog.show()
    }

    override fun doInBackground(vararg params: Void?): Void? {
        Thread.sleep(5000)
        return null
    }

    override fun onPostExecute(result: Void?) {
        //Deixei vazio para simular o fechamento do dialog assim como parceiro
    }

    fun dismiss() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }
}