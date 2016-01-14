package com.app.torbjornzetterlund.app;
 
import com.app.torbjornzetterlund.R;
import com.app.torbjornzetterlund.utils.PrefManager;

import com.app.torbjornzetterlund.utils.LruBitmapCache;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;
import java.util.Locale;

public class AppController extends Application {

	public static final String TAG = AppController.class.getSimpleName();

	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	LruBitmapCache mLruBitmapCache;

	private static AppController mInstance;
	private PrefManager pref;
    private static boolean activityVisible;

    // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = "UA-67462967-3";

    public static int GENERAL_TRACKER = 0;
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    public HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    @Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		if (Const.forceRTL) {
			updateLanguage(this, Const.forceRTLLang);
		}
		pref = new PrefManager(this);
        Log.d("AppController: ", "Application Created!");
	}

	@Override
    public void onLowMemory() {
		super.onLowMemory();
		// free your memory, clean cache for example
		//Toast.makeText(getApplicationContext(), "Application on Low memory.", Toast.LENGTH_LONG).show();
		Log.d("AppController: ", "Application on Low memory.");
	}


	public static synchronized AppController getInstance() {
		return mInstance;
	}

	public PrefManager getPrefManger() {
		if (pref == null) {
			pref = new PrefManager(this);
		}

		return pref;
	}

	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public ImageLoader getImageLoader() {
		getRequestQueue();
		if (mImageLoader == null) {
			getLruBitmapCache();
			mImageLoader = new ImageLoader(this.mRequestQueue, mLruBitmapCache);
		}

		return this.mImageLoader;
	}

	public LruBitmapCache getLruBitmapCache() {
		if (mLruBitmapCache == null)
			mLruBitmapCache = new LruBitmapCache();
		return this.mLruBitmapCache;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }


    public synchronized Tracker getTracker(Context context, TrackerName trackerName) {
        if (!mTrackers.containsKey(trackerName)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
            Tracker t = (trackerName == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : (trackerName == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            t.enableAdvertisingIdCollection(true);
            mTrackers.put(trackerName, t);
        }
        return mTrackers.get(trackerName);
    }
    /*
    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }
    */

    public static void updateLanguage(Context ctx, String lang) {

		Configuration cfg = new Configuration();
		String language = lang;

		if (TextUtils.isEmpty(language) && lang == null) {
			cfg.locale = Locale.getDefault();
			String tmp_locale = "";
			tmp_locale = Locale.getDefault().toString().substring(0, 2);
			//manager.SaveValueToSharedPrefs("force_locale", tmp_locale);

		} else if (lang != null) {
			cfg.locale = new Locale(lang);
			//manager.SaveValueToSharedPrefs("force_locale", lang);

		} else if (!TextUtils.isEmpty(language)) {
			cfg.locale = new Locale(language);
		}
		ctx.getResources().updateConfiguration(cfg, null);


	}

}