package com.app.torbjornzetterlund.utils;

import android.app.Activity;
import android.util.Log;

import com.app.torbjornzetterlund.app.AppController;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

public final class AnalyticsUtil {

    private AnalyticsUtil() {
    }

    public static void sendScreenName(Activity activity, String screenName) {
        sendScreenName(activity, AppController.TrackerName.APP_TRACKER, screenName);
    }

    public static void sendScreenName(Activity activity, AppController.TrackerName trackerName, String screenName) {
        Log.d("GAv4 Tracker", screenName + " now initialize");
        Tracker t = getTracker(activity, trackerName);
        t.setScreenName(screenName);
        t.send(new HitBuilders.ScreenViewBuilder().build());
        GoogleAnalytics.getInstance(activity).dispatchLocalHits();
    }

    public static void sendEvent(Activity activity, String category, String action, String label) {
        sendEvent(activity, AppController.TrackerName.APP_TRACKER, category, action, label);
    }

    public static void sendEvent(Activity activity, int categoryId, int actionId, int labelId) {
        sendEvent(activity, AppController.TrackerName.APP_TRACKER,
                activity.getString(categoryId), activity.getString(actionId), activity.getString(labelId));
    }

    public static void sendEvent(Activity activity, AppController.TrackerName trackerName, String category, String action, String label) {
        Tracker t = getTracker(activity, trackerName);
        t.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }

    public void trackException(Activity activity, Exception e) {
        if (e != null) {
            Tracker t = getTracker(activity, AppController.TrackerName.APP_TRACKER);

            t.send(new HitBuilders.ExceptionBuilder().setDescription(
                            new StandardExceptionParser(activity, null).getDescription(Thread.currentThread().getName(), e)
                    ).setFatal(false).build()
            );
        }
    }

    private static Tracker getTracker(Activity activity, AppController.TrackerName trackerName) {
        return ((AppController) activity.getApplication()).getTracker(activity, trackerName);
    }

}