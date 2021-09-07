import 'dart:async';

import 'package:flutter/services.dart';

enum CallbackType {
  onInitialized, onNewMessage, onChatWindowVisibilityChanged
}

typedef LiveChatCallback = void Function(CallbackType type, dynamic arguments);

class FlutterLiveChat {
  static const MethodChannel _channel =
      const MethodChannel('LiveChat');

  static Future<void> showChatWindow() async {
    await _channel.invokeMethod('showChatWindow');
  }

  static void setListener(LiveChatCallback callback) {
    _channel.setMethodCallHandler((MethodCall call) async {
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

  /*static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }*/
}
