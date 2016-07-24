package com.app.torbjornzetterlund.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * @author Torbjorn Zetterlund
 * @version 1.0.0
 * 
 */
public class InstanceIdListenerService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, GcmRegisterIntent.class);
        startService(intent);
    }
}