package com.mixpush.example

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.mixpush.core.MixPushMessage
import com.mixpush.core.MixPushPassThroughReceiver
import com.mixpush.core.MixPushPlatform

class MyPassThroughReceiver : MixPushPassThroughReceiver {
    override fun onRegisterSucceed(context: Context?, platform: MixPushPlatform?) {
        TODO("Not yet implemented")
    }

    override fun onReceiveMessage(context: Context?, message: MixPushMessage?) {
        TODO("Not yet implemented")
        Log.e("onReceiveMessage", "$message");
        //Toast.makeText(this, "$message", Toast.LENGTH_SHORT).show()
    }
}
