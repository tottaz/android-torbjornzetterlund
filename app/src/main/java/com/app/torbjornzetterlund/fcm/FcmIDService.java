package com.app.torbjornzetterlund.fcm;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

//import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * @author Torbjorn Zetterlund
 * @version 1.0.0
 * 
 */
public class FcmIDService extends FirebaseInstanceIdService {

    String LOG_TAG = "WP GCM";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(LOG_TAG, "Refreshed token: " + refreshedToken);
        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }
    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        Intent intent = new Intent(this, FcmRegisterIntent.class);
        startService(intent);
    }
}