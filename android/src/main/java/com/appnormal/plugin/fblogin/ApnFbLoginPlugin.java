package com.appnormal.plugin.fblogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

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
        final String accessToken;
        Map<String, String> params = call.arguments();

        if (params.containsKey("accessToken"))
            accessToken = params.get("accessToken");

        switch (call.method) {
            case "login":

                FacebookSdk.setApplicationId(params.get("appId"));
                FacebookSdk.setClientToken(params.get("clientSecret"));
                FacebookSdk.setAutoLogAppEventsEnabled(true);
                //noinspection deprecation
                FacebookSdk.sdkInitialize(activity.getApplicationContext());

                mCallbackManager = CallbackManager.Factory.create();
                LoginManager.getInstance().registerCallback(mCallbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                Log.d("Success", "Login");

                                AccessToken accessToken = loginResult.getAccessToken();
                                Map<String, Object> data = new HashMap<>();
                                data.put("accessToken", accessToken.getToken());
                                data.put("acceptedPermissions", TextUtils.join(",", accessToken.getPermissions().toArray()));
                                data.put("deniedPermissions", TextUtils.join(",", accessToken.getDeclinedPermissions().toArray()));
                                data.put("userId", accessToken.getUserId());
                                data.put("expiresIn", accessToken.getExpires().getTime());
                                result.success(data);
                            }

                            @Override
                            public void onCancel() {
                                result.error(TAG, "Cancelled", null);
                            }

                            @Override
                            public void onError(FacebookException exception) {
                                result.error(TAG, exception.getMessage(), null);
                            }
                        });

                LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile", "user_friends"));
                break;

            case "graph/me":

                GraphRequest request = GraphRequest.newMeRequest(
                        AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    result.success(JsonConverter.convertToMap(object));
                                } catch (JSONException e) {
                                    result.error(TAG, "Error", e.getMessage());
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email");
                request.setParameters(parameters);
                request.executeAsync();
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mCallbackManager != null)
            return mCallbackManager.onActivityResult(requestCode, resultCode, data);
        else
            return false;
    }
}
