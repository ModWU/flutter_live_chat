package com.gv.livechat.flutter_live_chat

import android.content.Context
import com.livechatinc.inappchat.ChatWindowView

class ChatWindowProxyView(context: Context, uiReadyListener: IUiReadyListener): ChatWindowView(context) {

    private var uiReadyListener :IUiReadyListener = uiReadyListener;

    override fun onUiReady() {
        super.onUiReady()
        uiReadyListener.onUiReady();
    }

}

interface IUiReadyListener {
    fun onUiReady()
}