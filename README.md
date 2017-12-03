# apn_fb_login

A flutter plugin project that allows you to easily connect to Facebook and get your profile information

## Setup

### iOS configuration

Edit your `Info.plist` and add add the following:

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

Change `##APPID##` and `##APPNAME##`

### Android configuration
None

## Usage

Import the lib:

    import 'package:apn_fb_login/apn_fb_login.dart';

Define a `FacebookConnect` object:

    final _facebookConnect = new FacebookConnect(
        appId: '##APPID##',
        clientSecret: '##APPSECRET##');

Authenticate like so:

    ApnFbLogin.login(_facebookConnect).then((FacebookOAuthToken token) {
      this.token = token;
    }).catchError((dynamic error) {
      print(error);
    });

Use the FacebookOAuthToken to get the graph/me result:

    ApnFbLogin.me().then((FacebookUser user) {
      this.user = user;
    }).catchError((dynamic error) {
      print(error);
    });

See the project in /example for a more complete example

## Flutter
For help getting started with Flutter, view our online
[documentation](http://flutter.io/).
