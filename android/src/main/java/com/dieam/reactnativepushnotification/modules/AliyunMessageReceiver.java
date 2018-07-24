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
    protected void onNotification(Context context, String s, String s1, Map<String, String> map) {
        super.onNotification(context, s, s1, map);
        WritableMap params = Arguments.createMap();
        params.putString("content", s1);
        params.putString("title", s);
        for (Map.Entry<String, String> entry: map.entrySet()) {
            params.putString(entry.getKey(), entry.getValue());
        }
        sendEvent(AliyunPushModule.getReactContext(), "onNotification", params);
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