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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;

import java.lang.ref.WeakReference;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

public class RNPushNotificationRegistrationService extends Service {

    private static final String TAG = "RNPushNotification";

    private static final String PROVIDER_FCM = "google";
    private static final String PROVIDER_ALIYUN = "aliyun";

    public RNPushNotificationRegistrationService() {
      super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean useAliyun = intent.getBooleanExtra("useAliyun", false);
        if (useAliyun) {
            try {
                getAliyunToken(getApplicationContext());
            }
            catch (Exception ex) {
                handleRegistrationFailure("Aliyun registration failed getting application context", PROVIDER_ALIYUN);
            }
        }
        else {
            try {
                getFCMToken(intent.getStringExtra("senderID"));
            }
            catch (Exception ex) {
                handleRegistrationFailure("FCM registration failed with intent " + intent, PROVIDER_ALIYUN);
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
        String deviceID = PushServiceFactory.getCloudPushService().getDeviceId();
        if (deviceID!=null && deviceID.length()>0) {
            sendRegistrationToken(deviceID, PROVIDER_ALIYUN);
        } else {
            try {
                // Since the token may not be received yet, start a thread to check for it
                // and send it to RN context when ready
                class AliyunTokenMonitor implements Runnable {
                    public void run() {
                        boolean found = false;
                        while(!found) {
                            try {
                                Thread.sleep(1000);
                                String deviceID = PushServiceFactory.getCloudPushService().getDeviceId();
                                if (deviceID != null && deviceID.length() > 0) {
                                    sendRegistrationToken(deviceID, PROVIDER_ALIYUN);
                                    found = true;
                                }
                            } catch (Exception e) {
                                handleRegistrationFailure("init cloudchannel AliyunTokenMonitor failed with exception:" + e.getMessage(), PROVIDER_ALIYUN);
                            }
                        }
                    }
                }
                Thread tokenMonitor = new Thread(new AliyunTokenMonitor());
                tokenMonitor.start();
            } catch (Exception e) {
                handleRegistrationFailure("init cloudchannel failed with exception:" + e.getMessage(), PROVIDER_ALIYUN);
            }
        }
    }

    public void sendRegistrationToken(String token, String provider) {
        Log.d(LOG_TAG, TAG + " Success: " + token);
        Intent intent = new Intent(this.getPackageName() + ".RNPushNotificationRegisteredToken");
        intent.putExtra("token", token);
        intent.putExtra("provider", provider);
        sendBroadcast(intent);
        stopSelf();
    }

    public void handleRegistrationFailure(String errorMsg, String provider) {
        Log.d(LOG_TAG, TAG + " Failed registering for " + provider + " push notifications with error " + errorMsg);
        Intent intent = new Intent(this.getPackageName() + ".RNPushNotificationRegistrationFailure");
        intent.putExtra("message", errorMsg);
        intent.putExtra("provider", provider);
        sendBroadcast(intent);
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
                String token = FirebaseInstanceId.getInstance().getToken();
                service.sendRegistrationToken(token, PROVIDER_FCM);
            } catch (Exception ex) {
                service.handleRegistrationFailure("Failed to get FCM token with exception: " + ex.getMessage(), PROVIDER_FCM);
            }
            return null;
        }
    }

    /**
     * Check to see if Google Play Services are available, e.g. for determining
     * which push provider to use
     * @param context a Context to feed to the Google SDK method
     * @return whether or not Play Services are available
     */
    public static boolean checkPlayServicesAvailable(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                Log.i(TAG, "Google Play Services availability check returned negative with code: " + resultCode);
            } else {
                Log.i(TAG, "Google Play Services availability check: this device is not supported.");
            }
            return false;
        }
        return true;
    }
}
