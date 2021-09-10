import 'dart:async';

import 'package:flutter/services.dart';

enum CallbackType { onInitialized, onNewMessage, onChatWindowVisibilityChanged }

typedef LiveChatCallback = void Function(CallbackType type, dynamic arguments);

class FlutterLiveChat {

  FlutterLiveChat(int viewId): channel = MethodChannel('LiveChat_$viewId');

  final MethodChannel channel;

  Future<void> showChatWindow() async {
    await channel.invokeMethod('showChatWindow');
  }

  void setListener(int viewId, LiveChatCallback callback) {
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

  /*static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }*/
}
