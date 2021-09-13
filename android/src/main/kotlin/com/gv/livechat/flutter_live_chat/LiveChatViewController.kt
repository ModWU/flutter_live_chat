package com.gv.livechat.flutter_live_chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.TextView
import com.livechatinc.inappchat.ChatWindowConfiguration
import com.livechatinc.inappchat.ChatWindowErrorType
import com.livechatinc.inappchat.ChatWindowView
import com.livechatinc.inappchat.ChatWindowView.ChatWindowEventsListener
import com.livechatinc.inappchat.models.NewMessageModel
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import java.util.*


class LiveChatViewController(messenger: BinaryMessenger, viewId: Int, args: Any?) :
        PlatformView, MethodChannel.MethodCallHandler, ChatWindowEventsListener, IActivityLifecycle {

    private val activity: Activity = PluginContext.activity as Activity;
    private val chatWindowView: ChatWindowView = ChatWindowView(activity);//ChatWindowView.createAndAttachChatWindowInstance(activity)
    private val notInitializedView: TextView = TextView(activity);
    private val methodChannel: MethodChannel = MethodChannel(messenger, "LiveChat_$viewId")

   // private var configuration: ChatWindowConfiguration? = null

    private var methodResult: MethodChannel.Result? = null

    private var isInitialized: Boolean = false

    init {
        //通信
        methodChannel.setMethodCallHandler(this)
        Log.d("kotlinDebugLog", "LiveChatViewController${hashCode()} init => args: $args, viewId: $viewId")

        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        notInitializedView.setGravity(Gravity.CENTER);
        notInitializedView.setLayoutParams(layoutParams);
        notInitializedView.setTextColor(activity.getResources().getColor(android.R.color.black));
        notInitializedView.setText("Initialization exception")
        if (args is Map<*, *>) {
            val licenceNumber: String = args["licenceNumber"] as String;
            val groupId: String = args["groupId"] as String;

            if (licenceNumber == null || groupId == null) {
                markNotInitialize("Initialization failed. Neither the 'licenceNumber'(=$licenceNumber) nor 'groupId'(=$groupId) parameters can be null.");
            } else {
                val visitorName: String? = args["visitorName"] as String?;
                val visitorEmail: String? = args["visitorEmail"] as String?;
                val customVariables: HashMap<String, String>? = args["customVariables"] as HashMap<String, String>?;

                Log.d("kotlinDebugLog", "LiveChatViewController init => licenceNumber: $licenceNumber, groupId: $groupId, visitorName: $visitorName, visitorEmail: $visitorEmail, customVariables: $customVariables")

                val configuration = ChatWindowConfiguration(licenceNumber, groupId, visitorName ?: "", visitorEmail ?: "", customVariables ?: HashMap<String, String>())

                chatWindowView.setUpWindow(configuration)
                chatWindowView.setUpListener(this)
                chatWindowView.initialize()

                isInitialized = true
                methodChannel.invokeMethod("onInitialized", mapOf(
                        "isSuccess" to true,
                        "message" to "Initialization succeeded."
                ))
            }
        } else {
            markNotInitialize("Initialization failed. Parameter type must be Map.")
        }
    }

    /*private fun startChatActivity() {
        //val intent: Intent = Intent(context, ChatWindowActivity.class)
        val intent = Intent(context, ChatWindowActivity::class.java)
        intent.putExtras((configuration as ChatWindowConfiguration).asBundle());
        context.startActivity(intent);
    }*/



    private fun markNotInitialize(message: String) {
        isInitialized = false

        Log.d("kotlinDebugLog", "markNotInitialize message: $message")
        notInitializedView.setText("$message")
        methodChannel.invokeMethod("onInitialized", mapOf(
                "isSuccess" to false,
                "message" to message
        ))
    }

    private fun notInitialized(result: MethodChannel.Result?) {
        result?.error(
                "LiveChatSDKError",
                "LiveChatSDK is not initialized",
                "Call 'LiveChatSDK.initialize' before this."
        )
    }

    private fun showChatWindow(call: MethodCall, result: MethodChannel.Result) {
        Log.d("kotlinDebugLog", "showChatWindow")
        methodResult = result
        chatWindowView.showChatWindow()
        //startChatActivity()
    }


    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (!isInitialized) {
            notInitialized(result)
            return
        }
        when (call.method) {
            "showChatWindow" -> {
                showChatWindow(call, result)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun getView(): View {
        Log.d("kotlinDebugLog", "getView isInitialized: $isInitialized, notInitializedView: $notInitializedView")
        //return if (isInitialized) chatWindowView else notInitializedView
        return chatWindowView
    }

    override fun dispose() {
        val webView: WebView? = chatWindowView.findViewById(R.id.chat_window_web_view)
        Log.d("kotlinDebugLog", "dispose => webView: $webView")
        chatWindowView.removeAllViews()
        if (webView != null) {
            val parent: ViewParent? = webView.parent
            if (parent != null) {
                (parent as ViewGroup).removeView(webView)
            }
            webView.stopLoading()
            //webView.settings.javaScriptEnabled = false
            //webView.clearHistory()
            //webView.clearCache(true)
            //webView.loadUrl("about:blank")
            //-----webView.pauseTimers()
            webView.removeJavascriptInterface("androidMobileWidget")
            webView.removeAllViews()
        }
    }

    override fun onChatWindowVisibilityChanged(visible: Boolean) {
        Log.d("kotlinDebugLog", "onChatWindowVisibilityChanged => visible: $visible")
        methodChannel.invokeMethod("onChatWindowVisibilityChanged", visible)
    }

    override fun onNewMessage(message: NewMessageModel?, windowVisible: Boolean) {
        Log.d("kotlinDebugLog", "onNewMessage => message: $message, windowVisible: $windowVisible")
        if (message != null) {
            methodChannel.invokeMethod("onNewMessage", mapOf(
                "messageType" to message.messageType,
                    "hasMessage" to true,
                "author" to message.author.name,
                    "id" to message.id,
                    "text" to message.text,
                    "timestamp" to message.timestamp,
                    "windowVisible" to windowVisible
            ))
        } else {
            methodChannel.invokeMethod("onNewMessage", mapOf(
                    "hasMessage" to false,
                    "windowVisible" to windowVisible
            ))
        }
    }

    override fun onStartFilePickerActivity(intent: Intent?, requestCode: Int) {
        Log.d("kotlinDebugLog", "onStartFilePickerActivity => requestCode: $requestCode")
        activity.startActivityForResult(intent, requestCode)
    }

    override fun onError(errorType: ChatWindowErrorType?, errorCode: Int, errorDescription: String?): Boolean {
        Log.d("kotlinDebugLog", "onError => errorType: $errorType, errorCode: $errorCode, errorDescription: $errorDescription")
        return true
    }

    override fun handleUri(uri: Uri?): Boolean {
        Log.d("kotlinDebugLog", "handleUri => uri: $uri")
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        chatWindowView.onActivityResult(requestCode, resultCode, data)
    }
}