package com.app.torbjornzetterlund.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.app.torbjornzetterlund.R;
import com.google.firebase.messaging.FirebaseMessagingService;

/**
 * @author Torbjorn Zetterlund
 * @version 1.0.0
 *
 * Receives the message from FCM and pushes the right notification
 */
//public class FcmListener extends FcmListenerService {
public class FcmListener extends FirebaseMessagingService {

	String LOG_TAG = "WP FCM";
	NotificationManager mNotificationManager;
	SharedPreferences prefs;
	String pkg;
	int icon;

//	@Override
//	public void onMessageReceived(RemoteMessage message){
//		String from = message.getFrom();
//		Map data = message.getData();

//	@Override
    public void onMessageReceived(String from, Bundle data) {
        prefs = getSharedPreferences("wp_fcm", 0);
        pkg = prefs.getString("pkg", null);
		icon = getResources().getIdentifier("fcm_notification_icon", "drawable", pkg);

		Log.e(LOG_TAG, pkg);

		if (!data.isEmpty()) {
			Log.e(LOG_TAG, "Message received, evaluating now");
	        if(data.getString("message") != null){
	    		sendMessageNotification(data.getString("message"));
	    	}else if(data.getString("update") != null) {
	    		sendUpdateNotification(data.getString("update"));
	    	}else if(data.getString("new_post") != null) {
	    		sendNewNotification(data.getString("new_post"));
	    	}
        }
    }

	//
	// Send Message Notification to phone
	//

	private void sendMessageNotification(String msg) {
		Log.e(LOG_TAG, "Message Notification");
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			
		Intent intent = new Intent(FcmListener.this, FcmIntentHandle.class);
		intent.putExtra("msg", msg);
		intent.putExtra("todo","message");
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_CANCEL_CURRENT);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notificate = prefs.getBoolean("notifications", true);
        boolean vibrate = prefs.getBoolean("notifications_vibrate", true);
        Uri uri = Uri.parse(prefs.getString("notifications_ringtone", "none"));
		Bitmap bm = BitmapFactory.decodeResource(getResources(), icon);
		
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				.setLights(Color.argb(255, 255, 0, 0), 5000, 5000)
                .setLargeIcon(bm)
//                .setSmallIcon(icon)
				.setSmallIcon(R.drawable.ic_launcher)
                .setSound(uri)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg)
                .setContentTitle(getString( getResources().getIdentifier("fcm_new_message_title", "string", pkg) ))
                .setContentIntent(contentIntent);
 		
        if (vibrate && notificate) {
        	mBuilder.setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_VIBRATE);
        	mNotificationManager.notify(0, mBuilder.build());
 		}else if(!vibrate && notificate){
 			mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
 			mNotificationManager.notify(0, mBuilder.build());
 		}
    }

	//
	// Send Post Update Notification
	//
    private void sendUpdateNotification(String post) {
		Log.e(LOG_TAG, "Post update");

		String[] ps = post.split(";");
    	String title = ps[0];
    	String url = ps[1];
    	String id = ps[2];
    	String author = ps[3];

		Intent intent = new Intent(FcmListener.this, FcmIntentHandle.class);
		intent.putExtra("todo","updatePost");
 		intent.putExtra("post_title", title);
 		intent.putExtra("post_url", url);
 		intent.putExtra("post_id", id);
 		intent.putExtra("post_author", author);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_CANCEL_CURRENT);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notificate = prefs.getBoolean("notifications", true);
        boolean vibrate = prefs.getBoolean("notifications_vibrate", true);
        Uri uri = Uri.parse(prefs.getString("notifications_ringtone", "none"));
 		Bitmap bm = BitmapFactory.decodeResource(getResources(), icon);
 		
 		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(bm)
				.setSmallIcon(R.drawable.ic_launcher)
                .setSound(uri)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText( getString( getResources().getIdentifier("fcm_update_post_text", "string", pkg) )+"\""+title+"\" ") )
                .setContentText( getString( getResources().getIdentifier("fcm_update_post_text", "string", pkg) )+"\""+title+"\" " )
                .setContentTitle(getString( getResources().getIdentifier("fcm_update_post_title", "string", pkg) ))
                .setContentIntent(contentIntent);
 		
        if (vibrate && notificate) {
        	mBuilder.setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_VIBRATE);
        	mNotificationManager.notify(0, mBuilder.build());
 		}else if(!vibrate && notificate){
 			mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
 			mNotificationManager.notify(0, mBuilder.build());
 		}
    }

	//
	// Send New Post Notfication
	//
    private void sendNewNotification(String post) {
		Log.e(LOG_TAG, "New Post");
		String[] ps = post.split(";");
    	String title = ps[0];
    	String url = ps[1];
    	String id = ps[2];
    	String author = ps[3];
    	
		Intent intent = new Intent(FcmListener.this, FcmIntentHandle.class);
		intent.putExtra("todo","updatePost");
 		intent.putExtra("post_title", title);
 		intent.putExtra("post_url", url);
 		intent.putExtra("post_id", id);
 		intent.putExtra("post_author", author);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_CANCEL_CURRENT);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notificate = prefs.getBoolean("notifications", true);
        boolean vibrate = prefs.getBoolean("notifications_vibrate", true);
        Uri uri = Uri.parse(prefs.getString("notifications_ringtone", "none"));
 		Bitmap bm = BitmapFactory.decodeResource(getResources(), icon);
 		
 		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(bm)
				.setSmallIcon(R.drawable.ic_launcher)
                .setSound(uri)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString( getResources().getIdentifier("fcm_new_post_text", "string", pkg))+" \""+title+"\""))
                .setContentText(getString( getResources().getIdentifier("fcm_new_post_text", "string", pkg))+" \""+title+"\"")
                .setContentTitle(getString( getResources().getIdentifier("fcm_new_post_title", "string", pkg)))
                .setContentIntent(contentIntent);
 		
        if (vibrate && notificate) {
        	mBuilder.setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_VIBRATE);
        	mNotificationManager.notify(0, mBuilder.build());
 		}else if(!vibrate && notificate){
 			mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
 			mNotificationManager.notify(0, mBuilder.build());
 		}
    }
}
