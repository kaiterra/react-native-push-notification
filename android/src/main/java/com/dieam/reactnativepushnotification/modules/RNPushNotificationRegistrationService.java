package com.dieam.reactnativepushnotification.modules;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

public class RNPushNotificationRegistrationService extends Service {

    private static final String TAG = "RNPushNotification";

    public RNPushNotificationRegistrationService() {
      super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Boolean USE_ALIYUN = true;
        if (USE_ALIYUN) {
            getAliyunToken(getApplicationContext());
        }
        else {
            getFCMToken(intent);
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }

    private void getFCMToken(Intent intent) {
        try {
            String SenderID = intent.getStringExtra("senderID");
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(SenderID,
              GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            sendRegistrationToken(token);
        } catch (Exception e) {
            Log.e(LOG_TAG, TAG + " failed to get FCM token with intent " + intent, e);
            stopSelf();
        }
    }

    /**
     * Register device with Aliyun push service
     * @param applicationContext
     */
    private void getAliyunToken(Context applicationContext) {
        final CloudPushService pushService = PushServiceFactory.getCloudPushService();
        pushService.register(applicationContext, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("ALIYUN PUSH", "init cloudchannel success.  DeviceID: " + pushService.getDeviceId());
                sendRegistrationToken(pushService.getDeviceId());
            }
            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("ALIYUN PUSH", "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
                stopSelf();
            }
        });
    }

    private void sendRegistrationToken(String token) {
        Intent intent = new Intent(this.getPackageName() + ".RNPushNotificationRegisteredToken");
        intent.putExtra("token", token);
        sendBroadcast(intent);
        stopSelf();
    }
}
