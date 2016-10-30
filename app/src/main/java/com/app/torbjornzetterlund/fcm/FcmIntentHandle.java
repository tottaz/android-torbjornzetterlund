package com.app.torbjornzetterlund.fcm;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


/**
 * @author Torbjorn Zetterlund)
 * @version 1.0.0
 *
 * This class handles what to do when the user presses the Notification
 * Override {@link #handle(String handle)} method to decide what to do with the message.
 * Override {@link #newPost()} method to change the handling of the newPost event
 * Override {@link #updatePost()} method to change the handling of the updatePost event
 * Override {@link #message()} method to change the handling of the message event
 */
public class FcmIntentHandle extends AppCompatActivity {

	String LOG_TAG = "WP GCM";
	String pkg;
	SharedPreferences prefs;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		prefs = getSharedPreferences("wp_gcm", 0);
		pkg = prefs.getString("pkg", null);

		String todo = getIntent().getStringExtra("todo");
		if(todo != null) {
			handle(todo);
		}
    }

	/**
	 * Decide what to do for each event.
	 * If you have custom events, Override this and add your events
	 */
	public void handle(String handle) {
		try {
			switch (handle) {
				case "newPost":
					newPost();
					break;
				case "updatePost":
					updatePost();
					break;
				case "message":
					message();
					break;
			}
		}catch(ClassNotFoundException e) {
			Log.e(LOG_TAG, e.toString());
		}
	}

    /**
	 * handle the newPost event.
	 * Override this method to change the handling of this event
	 */
    public void newPost() throws ClassNotFoundException {
    	String title = getIntent().getStringExtra("post_title");
    	String url = getIntent().getStringExtra("post_url");
    	String id = getIntent().getStringExtra("post_id");
    	String author = getIntent().getStringExtra("post_author");
    	
    	Intent intent = new Intent(FcmIntentHandle.this, Class.forName(prefs.getString("class", null)));
 		intent.putExtra("post_title", title);
 		intent.putExtra("post_url", url);
 		intent.putExtra("post_id", id);
 		intent.putExtra("post_author", author);
 		startActivity(intent);
 		finish();
    }

    /**
	 * Handle the updatePost event.
	 * Override this method to change the handling of this event
	 */
    public void updatePost() throws ClassNotFoundException {
    	String title = getIntent().getStringExtra("post_title");
    	String url = getIntent().getStringExtra("post_url");
    	String id = getIntent().getStringExtra("post_id");
    	String author = getIntent().getStringExtra("post_author");
    	
    	Intent intent = new Intent(FcmIntentHandle.this, Class.forName(prefs.getString("class", null)));
 		intent.putExtra("post_title", title);
 		intent.putExtra("post_url", url);
 		intent.putExtra("post_id", id);
 		intent.putExtra("post_author", author);
 		startActivity(intent);
 		finish();
    }

    /**
	 * Handle the message event.
	 * Override this method to change the handling of this event
	 */
    public void message() {
		String ok = getString(getResources().getIdentifier("gcm_dialog_ok", "string", pkg));
		String title = getString(getResources().getIdentifier("gcm_new_message_title", "string", pkg));
    	String ms = getIntent().getStringExtra("msg");
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder
			.setMessage(ms)
			.setCancelable(false)
			.setPositiveButton(ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					finish();
				}});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
    }
}