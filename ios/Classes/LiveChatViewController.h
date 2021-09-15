//
//  LiveChatViewController.h
//  flutter_live_chat
//
//  Created by ZhangYu on 2021/9/15.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

NS_ASSUME_NONNULL_BEGIN

@interface LiveChatViewController : NSObject <FlutterPlatformView>

- (instancetype)initWithFrame:(CGRect)frame
               viewIdentifier:(int64_t)viewId
                    arguments:(id _Nullable)args
              binaryMessenger:(NSObject<FlutterBinaryMessenger>*)messenger;

- (UIView *)view;

@end

NS_ASSUME_NONNULL_END
