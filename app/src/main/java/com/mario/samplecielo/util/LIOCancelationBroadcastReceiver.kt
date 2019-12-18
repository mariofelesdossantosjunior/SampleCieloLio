package com.mario.samplecielo.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cielo.orders.aidl.ParcelableOrder


class LIOCancelationBroadcastReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent) {
        val order = intent.extras?.getParcelable<ParcelableOrder>(INTENT_ORDER_KEY)

        order?.let {
            if (MY_ACCESS_KEY.equals(
                    it.accessKey,
                    ignoreCase = true
                ) && MY_CLIENT_ID.equals(it.accessKey, ignoreCase = true)
            ) {
                /*val transaction: ParcelableTransaction? = intent.extras?.getParcelable(INTENT_TRANSACTION_KEY)

                Log.d(TAG, "Transação cancelada: ${transaction?.id}")
                Log.d(TAG, "Transação cancelada: ${transaction?.cieloCode}")*/
            }
        }
    }

    companion object {
        const val MY_CLIENT_ID = "WqKoztAU7CNSdzqoIThyscctnYvoedarNQphbjLdY6MmZUdosb"
        const val MY_ACCESS_KEY = "Mi1Hijxvg1k6lftv3pEscEfTRSeMkp7Nfab2pVkH70QgZwECWd"

        private const val INTENT_ORDER_KEY = "ORDER"
        private const val INTENT_TRANSACTION_KEY = "TRANSACTION"
        private val TAG = LIOCancelationBroadcastReceiver::class.java.simpleName
    }
}