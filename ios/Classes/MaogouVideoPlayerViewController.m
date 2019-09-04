#import "MaogouVideoPlayerViewController.h"
#import <ZFPlayer/ZFPlayer.h>
#import <ZFPlayer/ZFAVPlayerManager.h>
#import <ZFPlayer/ZFPlayerControlView.h>
#import <ZFPlayer/ZFUtilities.h>
#import <ZFPlayer/UIImageView+ZFCache.h>

@interface MaogouVideoPlayerViewController ()
@property (nonatomic, strong) ZFPlayerController *player;
@property (nonatomic, strong) UIView *containerView;
@property (nonatomic, strong) ZFPlayerControlView *controlView;
@end

@implementation MaogouVideoPlayerViewController {
    int64_t _viewId;
    FlutterMethodChannel* _channel;
}

- (instancetype)initWithWithFrame:(CGRect)frame
                        viewIdentifier:(int64_t)viewId
                             arguments:(id _Nullable)args
                       binaryMessenger:(NSObject<FlutterBinaryMessenger>*)messenger {

    if ([super init]) {
        _viewId = viewId;
        _containerView = [UIView new];
        NSDictionary *dic = args;
        CGFloat x = [dic[@"x"] floatValue];
        CGFloat y = [dic[@"y"] floatValue];
        CGFloat width = [dic[@"width"] floatValue];
        CGFloat height = [dic[@"height"] floatValue];
        _containerView.frame = CGRectMake(x, y, width, height);

        ZFAVPlayerManager *playerManager = [[ZFAVPlayerManager alloc] init];

        _player = [ZFPlayerController playerWithPlayerManager:playerManager containerView: _containerView];
        
        _controlView = [ZFPlayerControlView new];
        _controlView.fastViewAnimated = YES;
        _controlView.autoHiddenTimeInterval = 5;
        _controlView.autoFadeTimeInterval = 0.5;
        _controlView.prepareShowLoading = YES;
        _controlView.prepareShowControlView = YES;
        
        _player.controlView = _controlView;
        _player.pauseWhenAppResignActive = YES;

        @weakify(self)
        _player.orientationWillChange = ^(ZFPlayerController * _Nonnull player, BOOL isFullScreen) {
            @strongify(self)
            [self setNeedsStatusBarAppearanceUpdate];
        };

        _player.playerDidToEnd = ^(id _Nonnull asset) {
            //@strongify(self)
            //[self.player.currentPlayerMannager replay];
            //[self.player playTheNext]
        };

        NSString* channelName = [NSString stringWithFormat:@"maogou_video_player_%lld", viewId];
        _channel = [FlutterMethodChannel methodChannelWithName:channelName binaryMessenger:messenger];
        __weak __typeof__(self) weakSelf = self;
        [_channel setMethodCallHandler:^(FlutterMethodCall* call, FlutterResult result) {
            [weakSelf onMethodCall:call result:result];
        }];
    }

    return self;
}

- (nonnull UIView *)view {
    return _containerView;
}

- (void)onMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([[call method] isEqualToString:@"loadURL"]) {
        [self onLoadURL:call result:result];
    } else if ([[call method] isEqualToString:@"getCurrentPosition"]) {
        NSTimeInterval cTime = self.player.currentTime;
        NSInteger t = cTime;
        result(@(t));
    } else if ([[call method] isEqualToString:@"pause"]) {
        [self.player.currentPlayerManager pause];
    } else if([[call method] isEqualToString:@"resume"]) {
        [self.player.currentPlayerManager play];
    } else if([[call method] isEqualToString:@"seekTo"]) {
        NSNumber* seek = call.arguments;
        NSTimeInterval s = [seek doubleValue];
        [self.player.currentPlayerManager seekToTime:s completionHandler:^(BOOL finished){
            NSLog(@"finished");
        }];
    } else if ([[call method] isEqualToString:@"progress"]) {
        result(@(self.player.progress));
    } else if ([[call method] isEqualToString:@"release"]) {
        [self.player.currentPlayerManager stop];
    } else {
        result(FlutterMethodNotImplemented);
    }
}

- (void)onLoadURL:(FlutterMethodCall*)call result:(FlutterResult)result {
    NSString* url = call.arguments[@"url"];
    NSNumber* seek = call.arguments[@"seek"];

    if (![self loadURL:url seek:seek]) {
        result([FlutterError errorWithCode:@"loadUrl_failed"
                                  message:@"Failed parsing the URL"
                                  details:[NSString stringWithFormat:@"URL was: '%@'", url]]);
    } else {
        result(nil);
    }
}

- (bool)loadURL:(NSString*)url seek:(NSNumber*)seek {
    NSURL* nsUrl = [NSURL URLWithString:url];
    NSTimeInterval s = [seek doubleValue];
    self.player.assetURL = nsUrl;
    self.player.playerReadyToPlay = ^(id  _Nonnull asset, NSURL * _Nonnull assetURL) {
        if(self.player.totalTime > 0 && self.player.totalTime > s) {
            [self.player seekToTime:s completionHandler:^(BOOL finished) {
                NSLog(@"finished");
            }];
        }
    };

    return true;
}

@end
