#import "ApnFbLoginPlugin.h"
#import <FBSDKCoreKit/FBSDKCoreKit.h>
#import <FBSDKLoginKit/FBSDKLoginKit.h>

@implementation ApnFbLoginPlugin {
    UIViewController *_viewController;
}

+ (void)registerWithRegistrar:(NSObject <FlutterPluginRegistrar> *)registrar {
    FlutterMethodChannel *channel = [FlutterMethodChannel
                                     methodChannelWithName:@"apn_fb_login"
                                     binaryMessenger:[registrar messenger]];
    UIViewController *viewController = [UIApplication sharedApplication].delegate.window.rootViewController;
    ApnFbLoginPlugin *instance = [[ApnFbLoginPlugin alloc] initWithViewController:viewController];
    [registrar addMethodCallDelegate:instance channel:channel];
}


- (instancetype)initWithViewController:(UIViewController *)viewController {
    self = [super init];
    if (self) {
        _viewController = viewController;
    }
    return self;
}

- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result {
    
    if ([@"logout" isEqualToString:call.method]) {
    
        [FBSDKAccessToken setCurrentAccessToken:nil];
        result(@{});
        
    }else if ([@"login" isEqualToString:call.method]) {
        
        [self doFacebookAuthWithResult: result andCallback:^(FBSDKAccessToken *accessToken){
            result(@{
                     @"accessToken": accessToken.tokenString,
                     @"acceptedPermissions": [accessToken.permissions.allObjects componentsJoinedByString:@","],
                     @"deniedPermissions": [accessToken.declinedPermissions.allObjects componentsJoinedByString:@","],
                     @"userId": accessToken.userID,
                     @"expiresIn": @((int)round(accessToken.expirationDate.timeIntervalSince1970)),
                     });
        }];
        
    } else if ([@"graph/me" isEqualToString:call.method]) {
        
        if([FBSDKAccessToken currentAccessToken]){
            [self doGraphMeRequest: result];
        }else{
            [self doFacebookAuthWithResult: result andCallback:^(FBSDKAccessToken *accessToken){
                [self doGraphMeRequest: result];
            }];
        }
        
    } else {
        result(FlutterMethodNotImplemented);
    }
}

- (void)doFacebookAuthWithResult: (FlutterResult) result andCallback:(void(^)(FBSDKAccessToken *)) callback {
    FBSDKLoginManager *login = [[FBSDKLoginManager alloc] init];
    if ([UIApplication.sharedApplication canOpenURL:[NSURL URLWithString:@"fb://"]]) {
        login.loginBehavior = FBSDKLoginBehaviorSystemAccount;
    }
    
    void (^handler)(FBSDKLoginManagerLoginResult *, NSError *)=^(FBSDKLoginManagerLoginResult *fbResult, NSError *error) {
        if (error) {
            NSLog(@"Unexpected login error: %@", error);
            NSString *alertMessage = error.userInfo[FBSDKErrorLocalizedDescriptionKey] ?: @"There was a problem logging in. Please try again later.";
            NSString *alertTitle = error.userInfo[FBSDKErrorLocalizedTitleKey] ?: @"Oops";
            [[[UIAlertView alloc] initWithTitle:alertTitle
                                        message:alertMessage
                                       delegate:nil
                              cancelButtonTitle:@"OK"
                              otherButtonTitles:nil] show];
        } else if (fbResult.token) {
            FBSDKAccessToken *accessToken = fbResult.token;
            callback(accessToken);
        } else {
            NSLog(@"Login Cancel");
        }
        
    };
    
    [login logInWithReadPermissions:@[@"public_profile", @"email"]
                 fromViewController:_viewController
                            handler:handler];
}

- (void)doGraphMeRequest: (FlutterResult) result {
    [[[FBSDKGraphRequest alloc] initWithGraphPath:@"me"
                                       parameters:@{@"fields": @"picture, name, email"}]
     startWithCompletionHandler:^(FBSDKGraphRequestConnection *connection, id userinfo, NSError *error) {
         if (!error) {
             
             dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0ul);
             dispatch_async(queue, ^(void) {
                 
                 dispatch_async(dispatch_get_main_queue(), ^{
                     
                     NSDictionary *userdata = userinfo;
                     result(userdata);
                     
                 });
             });
             
         } else {
             
             NSLog(@"%@", [error localizedDescription]);
         }
     }];
}

@end
