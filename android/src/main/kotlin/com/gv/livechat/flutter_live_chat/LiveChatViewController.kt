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
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.platform.PlatformView
import java.util.*

class LiveChatViewController(messenger: BinaryMessenger, viewId: Int, args: Any?) :
        PlatformView, MethodChannel.MethodCallHandler, ChatWindowEventsListener, PluginRegistry.ActivityResultListener, IUiReadyListener {

    private val activity: Activity = PluginContext.activity as Activity
    private val chatWindowView: ChatWindowProxyView = ChatWindowProxyView(activity, this)//ChatWindowView.createAndAttachChatWindowInstance(activity)
    private val notInitializedView: TextView = TextView(activity)
    private val methodChannel: MethodChannel = MethodChannel(messenger, "LiveChat_$viewId")

   // private var configuration: ChatWindowConfiguration? = null

    private var methodResult: MethodChannel.Result? = null

    private var isVerifiedParam: Boolean = false

    private var licenceNumber: String? = null
    private var groupId: String? = null
    private var visitorName: String? = null
    private var visitorEmail: String? = null

    init {
        //通信
        methodChannel.setMethodCallHandler(this)
        PluginContext.registrar?.addActivityResultListener(this)
        PluginContext.activityPluginBinding?.addActivityResultListener(this)
        Log.d("kotlinDebugLog", "LiveChatViewController${hashCode()} init => PluginContext.registrar: ${PluginContext.registrar}, PluginContext.activityPluginBinding: ${PluginContext.activityPluginBinding}")
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
                markParameterError("Initialization failed. Neither the 'licenceNumber'(=$licenceNumber) nor 'groupId'(=$groupId) parameters can be null.");
            } else {
                val visitorName: String? = args["visitorName"] as String?;
                val visitorEmail: String? = args["visitorEmail"] as String?;
                val customVariables: HashMap<String, String>? = args["customVariables"] as HashMap<String, String>?;

                Log.d("kotlinDebugLog", "LiveChatViewController init => licenceNumber: $licenceNumber, groupId: $groupId, visitorName: $visitorName, visitorEmail: $visitorEmail, customVariables: $customVariables")
                this.licenceNumber = licenceNumber
                this.groupId = groupId
                this.visitorName = visitorName
                this.visitorEmail = visitorEmail

                isVerifiedParam = true

                val configuration = ChatWindowConfiguration(licenceNumber, groupId, visitorName ?: "", visitorEmail ?: "", customVariables ?: HashMap<String, String>())

                chatWindowView.setUpWindow(configuration)
                chatWindowView.setUpListener(this)
                chatWindowView.initialize()
            }
        } else {
            markParameterError("Initialization failed. Parameter type must be Map.")
        }
    }

    /*private fun startChatActivity() {
        //val intent: Intent = Intent(context, ChatWindowActivity.class)
        val intent = Intent(context, ChatWindowActivity::class.java)
        intent.putExtras((configuration as ChatWindowConfiguration).asBundle());
        context.startActivity(intent);
    }*/

    private fun markParameterError(message: String) {
        Log.d("kotlinDebugLog", "markParameterError message: $message")
        notInitializedView.setText("$message")
        methodChannel.invokeMethod("onError", mapOf(
                "errorType" to "ParameterError",
                "errorCode" to -1,
                "errorDescription" to "'licenceNumber' nor 'groupId' parameters cannot be null."
        ))
    }

    private fun notVerifiedParam(result: MethodChannel.Result?) {
        result?.error(
                "LiveChatSDKError",
                "Parameter validation error.",
                "'licenceNumber' nor 'groupId' parameters cannot be null."
        )
    }

    /*private fun initialize(call: MethodCall, result: MethodChannel.Result) {
        Log.d("kotlinDebugLog", "initialize................")
        methodResult = result
        val chatWindowViewClass: KClass<ChatWindowView> = ChatWindowView::class
        chatWindowViewClass.members.forEach{
            Log.d("kotlinDebugLog", "it.name: ${it.name}")
            when(it.name) {
                "initialized" -> {
                    val kmp = it as KMutableProperty1<ChatWindowView, Boolean>
                    kmp.isAccessible = true
                    kmp.set(chatWindowView, false)
                    Log.d("kotlinDebugLog", "kmp: ${kmp.get(chatWindowView)}")
                }
            }
        }
        chatWindowView.initialize()
        result.success(null)
    }*/

    private fun showChatWindow(call: MethodCall, result: MethodChannel.Result) {
        Log.d("kotlinDebugLog", "showChatWindow")
        methodResult = result
        chatWindowView.showChatWindow()
        result.success(null)
    }

    private fun hideChatWindow(call: MethodCall, result: MethodChannel.Result) {
        Log.d("kotlinDebugLog", "hideChatWindow")
        methodResult = result
        chatWindowView.hideChatWindow()
        result.success(null)
    }

    private fun onBackPressed(call: MethodCall, result: MethodChannel.Result) {
        Log.d("kotlinDebugLog", "onBackPressed")
        methodResult = result
        result.success(chatWindowView.onBackPressed())
    }

    private fun isInitialized(call: MethodCall, result: MethodChannel.Result) {
        Log.d("kotlinDebugLog", "isInitialized")
        methodResult = result
        result.success(chatWindowView.isInitialized)
    }

    private fun isChatLoaded(call: MethodCall, result: MethodChannel.Result) {
        Log.d("kotlinDebugLog", "isChatLoaded")
        methodResult = result
        result.success(chatWindowView.isChatLoaded)
    }

    private fun reload(call: MethodCall, result: MethodChannel.Result) {
        Log.d("kotlinDebugLog", "reload")
        methodResult = result
        chatWindowView.reload()
        result.success(null)
    }

    private fun setUpWindow(call: MethodCall, result: MethodChannel.Result) {
        methodResult = result

        val customVariables: HashMap<String, String> = HashMap()
        if (call.arguments is Map<*, *>) {
            val arguments: Map<*, *> = call.arguments as Map<*, *>;
            for ((key, value) in arguments){
                customVariables["$key"] = "$value"
            }
        }
        Log.d("kotlinDebugLog", "setUpWindow 12=> call.arguments: ${call.arguments}, customVariables: $customVariables")
        val configuration = ChatWindowConfiguration(licenceNumber as String, groupId as String, visitorName ?: "", visitorEmail ?: "", customVariables)
        chatWindowView.setUpWindow(configuration)
        result.success(null)
    }


    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (!isVerifiedParam) {
            notVerifiedParam(result)
            return
        }
        when (call.method) {
            /*"initialize" -> {
                initialize(call, result)
            }*/
            "showChatWindow" -> {
                showChatWindow(call, result)
            }
            "hideChatWindow" -> {
                hideChatWindow(call, result)
            }
            "onBackPressed" -> {
                onBackPressed(call, result)
            }
            "isInitialized" -> {
                isInitialized(call, result)
            }
            "isChatLoaded" -> {
                isChatLoaded(call, result)
            }
            "reload" -> {
                reload(call, result)
            }
            "setUpWindow" -> {
                setUpWindow(call, result)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun getView(): View {
        Log.d("kotlinDebugLog", "getView isVerifiedParam: $isVerifiedParam, notInitializedView: $notInitializedView")
        //return if (isInitialized) chatWindowView else notInitializedView
        return chatWindowView
    }

    override fun dispose() {
        val webView: WebView? = chatWindowView.findViewById(R.id.chat_window_web_view)
        Log.d("kotlinDebugLog", "dispose => webView: $webView")
        PluginContext.activityPluginBinding?.removeActivityResultListener(this)
        methodChannel.setMethodCallHandler(null)
        chatWindowView.removeAllViews()
        if (webView != null) {
            val parent: ViewParent? = webView.parent
            if (parent != null) {
                (parent as ViewGroup).removeView(webView)
            }
            webView.stopLoading()
            webView.settings.javaScriptEnabled = false
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
        methodChannel.invokeMethod("onError", mapOf(
                "errorType" to "${errorType?.name}",
                "errorCode" to errorCode,
                "errorDescription" to errorDescription
        ))
        return true
    }

    override fun handleUri(uri: Uri?): Boolean {
        Log.d("kotlinDebugLog", "handleUri => uri: $uri")
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        Log.d("kotlinDebugLog", "onActivityResult => requestCode: $requestCode, resultCode: $resultCode")
        chatWindowView.onActivityResult(requestCode, resultCode, data)
        return false
    }

    override fun onUiReady() {
        methodChannel.invokeMethod("onUiReady", null)
    }

}