import 'dart:async';
import 'dart:core';

import 'package:flutter/services.dart';


class ApnFbLogin {
  static FacebookConnect _facebookConnect;
  static FacebookOAuthToken _facebookOAuthToken;
  static const MethodChannel _channel = const MethodChannel('apn_fb_login');

  static Future<String> get platformVersion =>
      _channel.invokeMethod('getPlatformVersion');

  static Future<FacebookOAuthToken> login(FacebookConnect facebookConnect) {
    _facebookConnect = facebookConnect;
    Map<String, dynamic> arguments = _facebookConnect.toMap();
    return _channel.invokeMethod("login", arguments).then((Map<String, String> data) {
      _facebookOAuthToken = new FacebookOAuthToken.fromMap(data);
      return _facebookOAuthToken;
    });
  }

  static Future<FacebookUser> me() {
    Map<String, dynamic> arguments = _facebookConnect.toMap()
      ..putIfAbsent("accessToken", () => _facebookOAuthToken.accessToken);

    return _channel.invokeMethod("graph/me", arguments)
        .then((Map<String, String> data) => new FacebookUser.fromMap(data));
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


class FacebookConnect {
  final String appId;
  final String clientSecret;

  static FacebookConnect _instance;

  FacebookConnect._({this.appId, this.clientSecret});

  factory FacebookConnect({String appId, String clientSecret}) =>
      _instance ??= new FacebookConnect._(appId: appId, clientSecret: clientSecret);

  Map toMap() => {'appId': appId, 'clientSecret': clientSecret};
}

class FacebookAuthScope {
  static String get publicProfile => "public_profile";

  static String get userFriends => "user_friends";

  static String get email => "email";

  static String get userAboutMe => "user_about_me";

  static String get userActionsBooks => "user_actions.books";

  static String get userActionsFitness => "user_actions.fitness";

  static String get userActionsMusic => "user_actions.music ";

  static String get userActionsNews => "user_actions.news";

  static String get userActionsVideo => "user_actions.video";

  static String get userBirthday => "user_birthday";

  static String get userEducationHistory => "user_education_history";

  static String get userEvents => "user_events";

  static String get userGamesActivity => "user_games_activity";

  static String get userHometown => "user_hometown";

  static String get userLikes => "user_likes";

  static String get userLocation => "user_location";

  static String get userManagedGroups => "user_managed_groups";

  static String get userPhotos => "user_photos";

  static String get userPosts => "user_posts";

  static String get userRelationships => "user_relationships";

  static String get userRelationshipDetails => "user_relationship_details";

  static String get userReligionPolitics => "user_religion_politics";

  static String get userTaggedPlaces => "user_tagged_places";

  static String get userVideos => "user_videos";

  static String get userWebsite => "user_website";

  static String get userWorkHistory => "user_work_history";

  static String get readCustomFriendlists => "read_custom_friendlists";

  static String get readInsights => "read_insights";

  static String get readAudienceNetworkInsights =>
      "read_audience_network_insights";

  static String get readPageMailboxes => "read_page_mailboxes";

  static String get managePages => "manage_pages";

  static String get publishPages => "publish_pages";

  static String get publishActions => "publish_actions";

  static String get rsvpEvent => "rsvp_event";

  static String get pagesShowList => "pages_show_list";

  static String get pagesManageCta => "pages_manage_cta";

  static String get pagesManageInstantArticles =>
      "pages_manage_instant_articles";

  static String get adsRead => "ads_read";

  static String get adsManagement => "ads_management";

  static String get businessManagement => "business_management";

  static String get pagesMessaging => "pages_messaging";

  static String get pagesMessagingSubscriptions =>
      "pages_messaging_subscriptions";

  static String get pagesMessagingPayments => "pages_messaging_payments";

  static String get pagesMessagingPhoneNumber => "pages_messaging_phone_number";

  static String userActions(String appNamespace) =>
      "user_actions:$appNamespace";
}
