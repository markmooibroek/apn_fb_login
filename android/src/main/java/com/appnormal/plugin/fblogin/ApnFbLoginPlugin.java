package com.appnormal.plugin.fblogin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.Gson;

import org.json.JSONException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * ApnFbLoginPlugin
 */
public class ApnFbLoginPlugin implements MethodCallHandler, PluginRegistry.ActivityResultListener {

    private static final String TAG = "ApnFbLoginPlugin";
    private static final String PREFS = "default";
    public static final String PREF_ACCESS_TOKEN = "access_token";

    private Activity activity;
    private CallbackManager mCallbackManager;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "apn_fb_login");
        final ApnFbLoginPlugin instance = new ApnFbLoginPlugin(registrar.activity());
        registrar.addActivityResultListener(instance);
        channel.setMethodCallHandler(instance);
    }

    private ApnFbLoginPlugin(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onMethodCall(final MethodCall call, final Result result) {
        Map<String, String> params = call.arguments();

        boolean loggedIn = getAccessToken() != null;
        if (loggedIn) {
            AccessToken.setCurrentAccessToken(getAccessToken());
        }

        Log.i(getClass().getSimpleName(), "onMethodCall: " + (loggedIn ? "true" : "false"));

        switch (call.method) {
            case "logout":
                saveAccessToken(null);
                AccessToken.setCurrentAccessToken(null);
                result.success(new HashMap<String, Object>());
                break;
            case "login":

                loginWithCallback(FacebookHelper.loginResultCallback(result, loginResult -> {
                    Log.d("Success", "Login");
                    saveAccessToken(loginResult.getAccessToken());

                    AccessToken accessToken = loginResult.getAccessToken();
                    Map<String, Object> data = new HashMap<>();
                    data.put("accessToken", accessToken.getToken());
                    data.put("acceptedPermissions", TextUtils.join(",", accessToken.getPermissions().toArray()));
                    data.put("deniedPermissions", TextUtils.join(",", accessToken.getDeclinedPermissions().toArray()));
                    data.put("userId", accessToken.getUserId());
                    data.put("expiresIn", accessToken.getExpires().getTime());
                    result.success(data);
                }));

                break;
            case "graph/me":
                if (!loggedIn) {
                    loginWithCallback(FacebookHelper.loginResultCallback(result, loginResult -> {
                        saveAccessToken(loginResult.getAccessToken());
                        queryMe(result);
                    }));
                } else {
                    queryMe(result);
                }
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void saveAccessToken(AccessToken accessToken) {
        activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(PREF_ACCESS_TOKEN, new Gson().toJson(accessToken))
                .apply();
    }

    private AccessToken getAccessToken() {
        boolean loggedIn = AccessToken.getCurrentAccessToken() == null;
        if (loggedIn) return AccessToken.getCurrentAccessToken();
        String savedAccessTokenString = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PREF_ACCESS_TOKEN, null);

        if (savedAccessTokenString != null) {
            return new Gson().fromJson(savedAccessTokenString, AccessToken.class);
        }
        return null;
    }

    private void loginWithCallback(FacebookCallback<LoginResult> callback) {
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, callback);
        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile", "email"));
    }

    private void queryMe(Result result) {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                (object, response) -> {
                    try {
                        result.success(JsonConverter.convertToMap(object));
                    } catch (JSONException e) {
                        result.error(TAG, "Error", e.getMessage());
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return mCallbackManager != null && mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
