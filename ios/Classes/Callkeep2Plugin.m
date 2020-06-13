#import "Callkeep2Plugin.h"
#if __has_include(<callkeep2/callkeep2-Swift.h>)
#import <callkeep2/callkeep2-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "callkeep2-Swift.h"
#endif

@implementation Callkeep2Plugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftCallkeep2Plugin registerWithRegistrar:registrar];
}
@end
