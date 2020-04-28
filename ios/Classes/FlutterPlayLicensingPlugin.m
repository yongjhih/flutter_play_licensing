#import "FlutterPlayLicensingPlugin.h"
#if __has_include(<flutter_play_licensing/flutter_play_licensing-Swift.h>)
#import <flutter_play_licensing/flutter_play_licensing-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_play_licensing-Swift.h"
#endif

@implementation FlutterPlayLicensingPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterPlayLicensingPlugin registerWithRegistrar:registrar];
}
@end
