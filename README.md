# apn_fb_login

A flutter plugin project that allows you to easily connect to Facebook and get your profile information

## Setup

### iOS configuration

Edit your `Info.plist` and add add the following:

```xml
    <key>CFBundleURLTypes</key>
    <array>
        <dict>
            <key>CFBundleURLSchemes</key>
            <array>
                <string>fb##APPID##</string>
            </array>
        </dict>
    </array>
    <key>FacebookAppID</key>
    <string>##APPID##</string>
    <key>FacebookDisplayName</key>
    <string>##APPNAME##</string>
    <key>LSApplicationQueriesSchemes</key>
    <array>
        <string>fbapi</string>
        <string>fb-messenger-api</string>
        <string>fbauth2</string>
        <string>fbshareextension</string>
    </array>
```

Don't forget to change `##APPID##` and `##APPNAME##`

### Android configuration

Add this to your AndroidManifest.xml

```xml
    <meta-data android:name="com.facebook.sdk.ApplicationId" android:value="##APPID##"/>
```

Don't forget to change `##APPID##`

## Usage

Import the lib:

```dart
    import 'package:apn_fb_login/apn_fb_login.dart';
```

Define a `FacebookConnect` object:

```dart
    final _facebookConnect = new FacebookConnect(
        appId: '##APPID##',
        clientSecret: '##APPSECRET##'
    );
```

Authenticate like so:

```dart
    ApnFbLogin.login(_facebookConnect).then((FacebookOAuthToken token) {
      this.token = token;
    }).catchError((dynamic error) {
      print(error);
    });
```

Use the `FacebookOAuthToken` to get the graph/me result:

```dart
    ApnFbLogin.me().then((FacebookUser user) {
      this.user = user;
    }).catchError((dynamic error) {
      print(error);
    });
```

See the project [example](https://github.com/markmooibroek/apn_fb_login/blob/master/example/lib/main.dart) for the complete example

## Flutter
For help getting started with Flutter, view our online
[documentation](http://flutter.io/).
