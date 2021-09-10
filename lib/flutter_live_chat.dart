import 'dart:async';

import 'package:flutter/services.dart';

enum CallbackType {
  onInitialized, onNewMessage, onChatWindowVisibilityChanged
}

typedef LiveChatCallback = void Function(CallbackType type, dynamic arguments);

class FlutterLiveChat {

  static Map<int, MethodChannel> _channelMap = {};

  static Future<void> showChatWindow(int viewId) async {
    await _channelMap[viewId]!.invokeMethod('showChatWindow');
  }

  static void addListener(int viewId, LiveChatCallback callback) {
    final MethodChannel channel = MethodChannel('LiveChat_$viewId');
    _channelMap[viewId] = channel;

    channel.setMethodCallHandler((MethodCall call) async {
      switch (call.method) {
        case 'onInitialized':
          callback(CallbackType.onInitialized, call.arguments);
          break;
        case 'onNewMessage':
          callback(CallbackType.onNewMessage, call.arguments);
          break;
        case 'onChatWindowVisibilityChanged':
          callback(CallbackType.onChatWindowVisibilityChanged, call.arguments);
          break;
      }
    });
  }

  static MethodChannel? removeListener(int viewId) {
    return _channelMap.remove(viewId);
  }

  /*static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }*/
}
