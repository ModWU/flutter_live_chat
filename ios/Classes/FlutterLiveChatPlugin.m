#import "FlutterLiveChatPlugin.h"
#if __has_include(<flutter_live_chat/flutter_live_chat-Swift.h>)
#import <flutter_live_chat/flutter_live_chat-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_live_chat-Swift.h"
#endif

#import "LiveChatViewFactory.h"

@implementation FlutterLiveChatPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    
    LiveChatViewFactory *factory = [[LiveChatViewFactory alloc] initWithMessenger:registrar.messenger];
    [registrar registerViewFactory:factory withId:@"live_chat_view"];
    
    [SwiftFlutterLiveChatPlugin registerWithRegistrar:registrar];
}
@end
