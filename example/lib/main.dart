import 'package:flutter/material.dart';
import 'package:apn_fb_login/apn_fb_login.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool isloading = false;
  FacebookUser user;
  String data;

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: new Text('Facebook login'),
          actions: <Widget>[
            new GestureDetector(
              onTap: _logout,
              child: new Icon(Icons.lock_open),
            )
          ],
        ),
        body: _body(),
        floatingActionButton: new FloatingActionButton(
          child: new Icon(Icons.face),
          onPressed: () {
            if (isloading) return;

            _setResult(true, null);
            _queryMe();
          },
        ),
      ),
    );
  }

  void _queryMe() {
    ApnFbLogin.me().then((FacebookUser user) {
      this.user = user;
      _setResult(false, user.toString());
    }).catchError((dynamic error) {
      print(error);
      _setResult(false, null);
    });
  }

  void _logout() {
    _setResult(true, null);
    ApnFbLogin.logout().then((dynamic data) {
      this.user = null;
      _setResult(false, null);
    }).catchError((dynamic error) {
      print(error);
      _setResult(false, null);
    });
  }

  Widget _body() {
    if (this.isloading) {
      return new Center(child: new CircularProgressIndicator());
    } else if (this.user != null) {
      return new Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            _textWidget("Got user!\nWelcome ${user.name}"),
            new ClipOval(child: new Image.network(this.user.avatarUrl)),
          ]);
    } else {
      return _textWidget("Click the button to connect with Facebook");
    }
  }

  void _setResult(bool loading, String data) {
    setState(() {
      this.isloading = loading;
      this.data = data;
    });
  }

  Widget _textWidget(String text) => new Container(
        padding: new EdgeInsets.all(8.0),
        child: new Center(
          child: new Text(
            text,
            textAlign: TextAlign.center,
          ),
        ),
      );
}
