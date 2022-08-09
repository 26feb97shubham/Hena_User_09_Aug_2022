package com.heena.user.broadcastreceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.heena.user.utils.Utility.isNetworkAvailable

class ConnectivityReceiver : BroadcastReceiver() {
    private var connectivityReceiverListener: ConnectivityReceiverListener? = null
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent?) {
        val status: Boolean = isNetworkAvailable()
        if (connectivityReceiverListener != null) {
            connectivityReceiverListener!!.onNetworkConnectionChanged(status)
        }else{
            connectivityReceiverListener!!.onNetworkConnectionChanged(status)
        }
    }
    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    fun NetworkChangeReceiver(connectivityReceiverListener: ConnectivityReceiverListener?) {
        this.connectivityReceiverListener = connectivityReceiverListener
    }
}