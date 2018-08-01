package com.dieam.reactnativepushnotification.modules;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.sdk.android.push.MessageReceiver;
import com.alibaba.sdk.android.push.notification.CPushMessage;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Map;

public class AliyunMessageReceiver extends MessageReceiver {
    public AliyunMessageReceiver() {
        super();
    }
    @Override
    protected void onMessage(Context context, CPushMessage cPushMessage) {
        super.onMessage(context, cPushMessage);
        WritableMap params = Arguments.createMap();
        params.putString("messageId", cPushMessage.getMessageId());
        params.putString("content", cPushMessage.getContent());
        params.putString("title", cPushMessage.getTitle());
        sendEvent(AliyunPushModule.getReactContext(), "onMessage", params);
    }

    @Override
    protected void onNotification(Context context, String title, String body, Map<String, String> map) {
        super.onNotification(context, title, body, map);
        WritableMap params = Arguments.createMap();
        params.putString("content", body);
        params.putString("title", title);
        for (Map.Entry<String, String> entry: map.entrySet()) {
            params.putString(entry.getKey(), entry.getValue());
        }
        sendEvent(AliyunPushModule.getReactContext(), "onNotification", params);
    }

    @Override
    protected void onNotificationOpened(Context var1, String var2, String var3, String var4) {
        Log.i(MessageReceiver.TAG, "notification opened");
    }

    @Override
    protected void onNotificationRemoved(Context var1, String var2) {
        Log.i(MessageReceiver.TAG, "notification removed");
    }

    @Override
    protected void onNotificationReceivedInApp(Context var1, String var2, String var3, Map<String, String> var4, int var5, String var6, String var7) {
        Log.i(MessageReceiver.TAG, "notification received");
    }

    @Override
    protected void onNotificationClickedWithNoAction(Context var1, String var2, String var3, String var4) {
        Log.i(MessageReceiver.TAG, "notification clicked with no action");
    }

    @Override
    protected void onConnectionStatusChanged(boolean var1) {
        Log.i(MessageReceiver.TAG, "connection status changed");
    }

    private void sendEvent(ReactContext context, String eventName, @Nullable WritableMap params) {
        if (context == null) {
            Log.i(MessageReceiver.TAG, "reactContext==null");
        }else{
            context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        }
    }
}