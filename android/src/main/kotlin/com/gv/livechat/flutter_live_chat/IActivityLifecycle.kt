package com.gv.livechat.flutter_live_chat

import android.content.Intent

interface IActivityLifecycle {
   fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}