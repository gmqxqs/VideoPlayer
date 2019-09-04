#import "VideoViewFactory.h"
#import "MaogouVideoPlayerViewController.h"

@implementation VideoViewFactory {
    NSObject<FlutterBinaryMessenger>* _messenger;
}

- (instancetype)initWithMessenger:(NSObject<FlutterBinaryMessenger>*)messenger {
    self = [super init];
    if (self) {
        _messenger = messenger;
    }

    return self;
}

- (NSObject<FlutterMessageCodec>*)createArgsCodec {
    return [FlutterStandardMessageCodec sharedInstance];
}

- (nonnull NSObject<FlutterPlatformView> *)createWithFrame:(CGRect)frame
                                            viewIdentifier:(int64_t)viewId
                                            arguments:(id _Nullable)args {
    MaogouVideoPlayerViewController* viewController = 
        [[MaogouVideoPlayerViewController alloc] initWithWithFrame:frame
                                                   viewIdentifier:viewId
                                                         arguments:args
                                                   binaryMessenger:_messenger];

    return viewController;
}

@end
