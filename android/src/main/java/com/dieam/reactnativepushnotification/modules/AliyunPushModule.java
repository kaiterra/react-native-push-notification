package com.dieam.reactnativepushnotification.modules;

import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;


public class AliyunPushModule extends ReactContextBaseJavaModule {
    private static ReactContext context;

    public AliyunPushModule(ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;
    }

    public static ReactContext getReactContext() {
        return context;
    }

    @Override
    public String getName() {
        return "AliyunPush";
    }

    @ReactMethod
    public void getDeviceId(Callback callback) {
        callback.invoke(PushServiceFactory.getCloudPushService().getDeviceId());
    }

    @ReactMethod
    public void bindAccount(String account, final Callback callback) {
        PushServiceFactory.getCloudPushService().bindAccount(account, new CommonCallback() {
            @Override
            public void onSuccess(String s) {
                callback.invoke("bind account success");
            }
            @Override
            public void onFailed(String s, String s1) {
                callback.invoke("bind account failed. errorCode:" + s + ", errorMsg:" + s1);
            }
        });
    }
}
