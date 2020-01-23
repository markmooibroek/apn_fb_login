package com.appnormal.plugin.fblogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

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
    private Activity activity;
    private CallbackManager mCallbackManager;

    private ApnFbLoginPlugin(Activity activity) {
        this.activity = activity;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "apn_fb_login");
        final ApnFbLoginPlugin instance = new ApnFbLoginPlugin(registrar.activity());
        registrar.addActivityResultListener(instance);
        channel.setMethodCallHandler(instance);
    }

    @Override
    public void onMethodCall(@NonNull final MethodCall call, @NonNull final Result result) {

        boolean isLoggedIn = AccessToken.getCurrentAccessToken() != null;

        switch (call.method) {
            case "logout":
                AccessToken.setCurrentAccessToken(null);
                result.success(new HashMap<String, Object>());
                break;
            case "login":

                loginWithCallback(FacebookHelper.loginResultCallback(result, loginResult -> {
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

                if (isLoggedIn) {
                    queryMe(result);
                } else {
                    loginWithCallback(FacebookHelper.loginResultCallback(result, loginResult -> {
                        queryMe(result);
                    }));
                }
                break;
            default:
                result.notImplemented();
                break;
        }
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
