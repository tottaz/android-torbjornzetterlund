package com.app.torbjornzetterlund.fcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.app.torbjornzetterlund.app.Const;
import com.google.firebase.iid.FirebaseInstanceId;

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
public class FcmRegisterIntent extends IntentService {

	String LOG_TAG = "WP FCM";
	String token, fcmurl, senderId;
	SharedPreferences prefs;
//    Context context;

	public FcmRegisterIntent() {
		super("RegIntent");
	}

	@Override
    protected void onHandleIntent(Intent intent) {
        Log.w(LOG_TAG, "Starting registration");

		String token = "";
		for (int i = 0; i < 10; i++) {
			token = FirebaseInstanceId.getInstance().getToken();
			if(!TextUtils.isEmpty(token)) break;
		}

		Log.d("FCM_TOKEN", token);

		sendRegistrationToServer(token);
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

	private void sendRegistrationToServer(String token) {

		String os = android.os.Build.VERSION.RELEASE;
		String model = getDeviceName();
		String serialno = Build.SERIAL;
		os = os.replaceAll(" ", "%20");
		model = model.replaceAll(" ", "%20");
		serialno = serialno.replaceAll(" ", "%20");
		// Categories request to get list of featured categories
		String fcmurl = Const.URL_REGISTER_DEVICE;
		fcmurl += "?regid="+ token + "&serialno=" + serialno + "&model="+ model + "&os=Android%20"+ os;

		try {
			URL url = new URL(fcmurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
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