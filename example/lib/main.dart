import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter_live_chat/flutter_live_chat.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key? key}) : super(key: key);

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const StandardMessageCodec _codec = StandardMessageCodec();

  late AndroidViewController _androidViewController;

  bool isVisible = false;

  int unreadCount = 0;

  @override
  void initState() {
    super.initState();
    _androidViewController = PlatformViewsService.initSurfaceAndroidView(
      id: hashCode,
      viewType: "live_chat_view",
      layoutDirection: TextDirection.ltr,
      creationParams: {
        "licenceNumber": "13076961",
        "groupId": "0",
        "visitorName": "abc123",
        "visitorEmail": "2398700195@qq.com",
        "customVariables": {
          "from": "goodsDetail",
          "uri": "https://www.baidu.com/"
        }
      },
      creationParamsCodec: _codec,
    );
    _androidViewController.create();

    FlutterLiveChat.addListener(hashCode, (CallbackType type, dynamic arguments) {
      switch (type) {
        case CallbackType.onInitialized:
          final bool isSuccess = arguments['isSuccess'];
          final String message = arguments['message'];
          print('FlutterLiveChat initialized => isSuccess: $isSuccess, message: $message');
          break;

        case CallbackType.onNewMessage:
          final bool hasMessage = arguments['hasMessage'];
          final bool windowVisible = arguments['windowVisible'];

          print(
              'onNewMessage => hasMessage: $hasMessage, windowVisible: $windowVisible');
          if (hasMessage && !windowVisible) {
            setState(() {
              unreadCount++;
            });
          }
          break;

        case CallbackType.onChatWindowVisibilityChanged:
          final bool visible = arguments;
          setState(() {
            if (visible) {
              unreadCount = 0;
            }
            isVisible = visible;
          });
          break;
      }
    });

  }

  @override
  void dispose() {
    FlutterLiveChat.removeListener(hashCode);
    _androidViewController.dispose();
    super.dispose();
  }

  void _showLiveChat() {
    FlutterLiveChat.showChatWindow(hashCode);
  }

  // return new ChatWindowConfiguration("1520", "77", "Android Widget Example", "985477819@qq.com", null);
  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text("LiveChat"),
          toolbarHeight: 0,
        ),
        resizeToAvoidBottomInset: false,
        body: AndroidViewSurface(
          controller: _androidViewController,
          hitTestBehavior: PlatformViewHitTestBehavior.opaque,
          gestureRecognizers: <Factory<OneSequenceGestureRecognizer>>[
            Factory<OneSequenceGestureRecognizer>(
              () => EagerGestureRecognizer(),
            ),
          ].toSet(),
        ),
        floatingActionButton: isVisible
            ? null
            : Stack(
                children: [
                  Padding(
                    padding: EdgeInsets.all(2),
                    child: FloatingActionButton(
                      onPressed: _showLiveChat,
                      tooltip: 'liveChat',
                      child: Icon(Icons.message),
                    ),
                  ),
                  if (unreadCount > 0)
                    Positioned(
                      child: Container(
                        height: 20,
                        width: 20,
                        alignment: Alignment.center,
                        decoration: BoxDecoration(
                          color: Colors.redAccent,
                          borderRadius: BorderRadius.circular(10),
                        ),
                        child: Text(
                          unreadCount <= 99 ? "$unreadCount" : "+99",
                          textAlign: TextAlign.center,
                          style: TextStyle(color: Colors.white, fontSize: 10),
                        ),
                      ),
                      right: 4,
                      top: 4,
                    )
                ],
              ) // This trailing comma makes auto-formatting nicer for build methods.
        );
  }
}
