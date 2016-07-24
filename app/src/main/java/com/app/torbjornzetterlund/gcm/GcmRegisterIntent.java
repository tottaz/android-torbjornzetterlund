package com.app.torbjornzetterlund.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Torbjorn Zetterlund
 * @version 1.0.0
 *
 * Register the Device in your WordPress Blog using your senderId
 */
public class GcmRegisterIntent extends IntentService {

	String LOG_TAG = "WP GCM";
	String token, gcmurl, senderId;
	SharedPreferences prefs;
//    Context context;

	public GcmRegisterIntent() {
		super("RegIntent");
	}

	@Override
    protected void onHandleIntent(Intent intent) {
        Log.w(LOG_TAG, "Starting registration");
		prefs = getSharedPreferences("wp_gcm", 0);

        gcmurl = getString( getResources().getIdentifier("gcm_url", "string", getApplicationContext().getPackageName()) );
        senderId = getString( getResources().getIdentifier("gcm_sender_id", "string", getApplicationContext().getPackageName()) );

		try {
            synchronized("RegIntent") {
            	InstanceID instanceID = InstanceID.getInstance(this);
            	token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
				if(token != null) {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putBoolean("doGcm", false);
					editor.putString("GcmToken", token);
					editor.apply();
					register();
				}
            }
		}catch(IOException e) {
			Log.e(LOG_TAG, e.toString());
		}
	}
	
	// Get the device model name with manufacturer name
	public String getDeviceName() {
	    String manufacturer = Build.MANUFACTURER;
	    String model = Build.MODEL;
	    if(model.startsWith(manufacturer)) {
	        return capitalize(model);
	    }else {
            return capitalize(manufacturer) + " " + model;
	    }
	}

	private String capitalize(String s) {
	    if(s == null || s.length() == 0) {
	        return "";
	    }
	    char first = s.charAt(0);
	    if(Character.isUpperCase(first)) {
	        return s;
	    }else {
	        return Character.toUpperCase(first) + s.substring(1);
	    }
	}

	//
	//  Register the device details
	//

	private void register() {
		String os = android.os.Build.VERSION.RELEASE;
		String model = getDeviceName();
		os = os.replaceAll(" ", "%20");
		model = model.replaceAll(" ", "%20");
		gcmurl += "?regId="+ token + "&os=Android%20"+ os + "&model="+ model;

		try {
			URL url = new URL(gcmurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.connect();

			final int statusCode = conn.getResponseCode();
			if(statusCode != 200) {
				Log.e(LOG_TAG, "Error " + statusCode + " for URL " + url.toExternalForm());
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
            Log.w(LOG_TAG, "Registration result: "+ response.toString());
		}catch(IOException e) {
            Log.e(LOG_TAG, e.toString());
        }
	}
}