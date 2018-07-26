package com.dieam.reactnativepushnotification.modules;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.lang.ref.WeakReference;

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
            try {
                getAliyunToken(getApplicationContext());
            }
            catch (Exception ex) {
                handleRegistrationFailure("Aliyun registration failed getting application context");
            }
        }
        else {
            try {
                getFCMToken(intent.getStringExtra("senderID"));
            }
            catch (Exception ex) {
                handleRegistrationFailure("FCM registration failed with intent " + intent);
            }
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }

    /**
     * Register device with Firebase cloud messaging
     * @param senderID
     */
    private void getFCMToken(String senderID) {
        RegisterFCMAsyncTask myTask = new RegisterFCMAsyncTask(this, senderID);
        myTask.execute();
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
                sendRegistrationToken(pushService.getDeviceId());
            }
            @Override
            public void onFailed(String errorCode, String errorMessage) {
                handleRegistrationFailure("init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });
    }

    public void sendRegistrationToken(String token) {
        Log.d(LOG_TAG, TAG + " Success: " + token);
        Intent intent = new Intent(this.getPackageName() + ".RNPushNotificationRegisteredToken");
        intent.putExtra("token", token);
        sendBroadcast(intent);
        stopSelf();
    }

    public void handleRegistrationFailure(String errorMsg) {
        Log.d(LOG_TAG, TAG + " " + errorMsg);
        stopSelf();
    }

    private static class RegisterFCMAsyncTask extends AsyncTask<Void, Void, String> {

        private WeakReference<RNPushNotificationRegistrationService> serviceWeakReference;
        private String SenderID;

        RegisterFCMAsyncTask(RNPushNotificationRegistrationService registrationService,
                             String senderID) {
            serviceWeakReference = new WeakReference<>(registrationService);
            SenderID = senderID;
        }

        @Override
        protected String doInBackground(Void... params) {
            RNPushNotificationRegistrationService service = serviceWeakReference.get();
            try {
                InstanceID instanceID = InstanceID.getInstance(service);
                String token = instanceID.getToken(SenderID,
                  GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                service.sendRegistrationToken(token);
            } catch (Exception ex) {
                service.handleRegistrationFailure("failed to get FCM token");
            }
            return null;
        }
    }
}
