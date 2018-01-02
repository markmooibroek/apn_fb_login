import 'dart:async';
import 'dart:core';

import 'package:flutter/services.dart';

class ApnFbLogin {
  static FacebookOAuthToken _facebookOAuthToken;
  static const MethodChannel _channel = const MethodChannel('apn_fb_login');

  static Future<FacebookOAuthToken> login() {
    Map<String, dynamic> arguments;
    return _channel
        .invokeMethod("login", arguments)
        .then((Map<String, String> data) {
      _facebookOAuthToken = new FacebookOAuthToken.fromMap(data);
      return _facebookOAuthToken;
    });
  }

  static Future<FacebookUser> me() {
    Map<String, dynamic> arguments;
    return _channel
        .invokeMethod("graph/me", arguments)
        .then((Map<String, String> data) => new FacebookUser.fromMap(data));
  }

  static Future<dynamic> logout() {
    Map<String, dynamic> arguments;
    return _channel.invokeMethod("logout", arguments);
  }
}

class FacebookUser {
  String id;
  String name;
  String email;

  FacebookUser.fromMap(Map<String, dynamic> data)
      : id = data["id"],
        name = data["name"],
        email = data["email"];

  String get avatarUrl => "https://graph.facebook.com/$id/picture?type=normal";

  @override
  String toString() {
    return 'FacebookUser{name: $name, email: $email}';
  }
}

class FacebookOAuthToken {
  String accessToken;
  List<String> acceptedPermissions;
  List<String> deniedTermissions;
  String userId;
  int expiresIn;

  FacebookOAuthToken.fromMap(Map<String, dynamic> data)
      : accessToken = data["accessToken"],
        acceptedPermissions = "${data["acceptedPermissions"]}".split(","),
        deniedTermissions = "${data["deniedTermissions"]}".split(","),
        userId = data["userId"],
        expiresIn = data["expiresIn"];

  @override
  String toString() {
    return 'FacebookOAuthToken{accessToken: $accessToken, acceptedPermissions: $acceptedPermissions, deniedTermissions: $deniedTermissions, userId: $userId, expiresIn: $expiresIn}';
  }
}
