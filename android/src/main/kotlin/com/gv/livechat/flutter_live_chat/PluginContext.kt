package com.gv.livechat.flutter_live_chat

import android.app.Activity
import android.content.Context
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.PluginRegistry
import java.lang.ref.WeakReference

internal object PluginContext {

    private var weakReferenceActivity: WeakReference<Activity?> = WeakReference(null)
    var activity: Activity?
        get() = weakReferenceActivity.get()
        set(value) {
            weakReferenceActivity = WeakReference(value)
        }

    private var weakReferenceContext: WeakReference<Context?> = WeakReference(null)
    var context: Context?
        get() = weakReferenceContext.get() ?: activity
        set(value) {
            weakReferenceContext = WeakReference(value)
        }

    private var weakReferenceRegistrar: WeakReference<PluginRegistry.Registrar?> = WeakReference(null)
    var registrar: PluginRegistry.Registrar?
        get() = weakReferenceRegistrar.get()
        set(value) {
            weakReferenceRegistrar = WeakReference(value)
        }

    private var weakReferenceActivityPluginBinding: WeakReference<ActivityPluginBinding?> = WeakReference(null)
    var activityPluginBinding: ActivityPluginBinding?
        get() = weakReferenceActivityPluginBinding.get()
        set(value) {
            weakReferenceActivityPluginBinding = WeakReference(value)
        }
}