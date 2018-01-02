package com.appnormal.plugin.fblogin;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;

import io.flutter.plugin.common.MethodChannel;

/**
 * Created by Mark Nicepants on 12-29-2017 - 22:01
 * <p>
 * This code may not be used without explicit permission of appnormal.com BV. This code is copyrighted.
 */

public class FacebookHelper {
    private static final String TAG = "FacebookHelper";

    public interface OnLoginSuccessListener {
        void onLoginResult(LoginResult loginResult);
    }

    public static FacebookCallback<LoginResult> loginResultCallback(final MethodChannel.Result result, final OnLoginSuccessListener onLoginSuccess) {
        return new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                onLoginSuccess.onLoginResult(loginResult);
            }

            @Override
            public void onCancel() {
                result.error(TAG, "Cancelled", null);
            }

            @Override
            public void onError(FacebookException exception) {
                result.error(TAG, exception.getMessage(), null);
            }
        };
    }
}
