package com.app.torbjornzetterlund;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.app.torbjornzetterlund.app.AppController;
import com.app.torbjornzetterlund.app.Category;
import com.app.torbjornzetterlund.app.Const;
import com.app.torbjornzetterlund.utils.AnalyticsUtil;
import com.app.torbjornzetterlund.utils.ConnectionDetector;
import com.app.torbjornzetterlund.utils.Utils;
import com.google.android.gms.analytics.GoogleAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends Activity {
    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final String
            TAG_CATEGORIES = "categories",
            TAG_TERM_ID = "id",
            TAG_TERM_NAME = "name";

    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //Forcing RTL Layout, If Supports and Enabled from Const file
        Utils.forceRTLIfSupported(this);

        //Get a Tracker (should auto-report)
        if (Const.Analytics_ACTIVE) {
            AnalyticsUtil.sendScreenName(this, TAG);
        }

        TextView versionTxt = (TextView) findViewById(R.id.AppVersion);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionTxt.setText("Version " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            //Best effort
        }
        checkInternetConnection();

    }

    public void checkInternetConnection (){
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            loadCategoriesData();
        } else {
            showAlertDialog(SplashActivity.this, getString(R.string.no_internet), getString(R.string.no_internet_message), false);
        }
    }

    public void loadCategoriesData(){
        // Categories request to get list of featured categories
        String url = Const.URL_BLOG_CATEGORIES;

        // Preparing volley's json object request
        JsonArrayRequest jsonArrReq = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                Log.d(TAG, response.toString());
                List<Category> categories = new ArrayList<Category>();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject catObj = response.getJSONObject(i);
//                            JSONObject catObj = (JSONObject) response.get(i);
                        // album id
                        String catID = catObj.getString(TAG_TERM_ID);

                        // album title
                        String catTitle = catObj.getString(TAG_TERM_NAME);

                        Category category = new Category();
                        category.setId(catID);
                        category.setTitle(catTitle);

                        // add album to list
                        categories.add(category);



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                // Store categories in shared pref
                AppController.getInstance().getPrefManger().storeCategories(categories);

                // String the main activity
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                // closing spalsh activity
                finish();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(TAG, "System Error: " + error.getMessage());

                // show error toast
                Toast.makeText(getApplicationContext(), getString(R.string.server_unavailable), Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), "System Error: " + error.getMessage(), Toast.LENGTH_LONG).show();

                // closing splash activity
                finish();
            }
        }) {

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
//                headers.put("Per_Page", Const.Per_Page);
                return headers;
            }

        };

        // disable the cache for this request, so that it always fetches updated
        // json
        jsonArrReq.setShouldCache(false);

        // Making the request
        AppController.getInstance().addToRequestQueue(jsonArrReq);
    }

    public void showAlertDialog(Context context, String title, String message, Boolean status) {

        AlertDialog alertDialog = new AlertDialog.Builder(
                context,
                R.style.AlertDialogCustom_Destructive)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Delete Action
                        dialogInterface.cancel();
                        finish();
                    }
                })
                .setNegativeButton("Re-check", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkInternetConnection();
                        dialogInterface.cancel();
                    }
                })
                .setTitle(title).setMessage(message).create();
        alertDialog.show();
    }


    private void addShortcut() {
        //Adding shortcut for MainActivity
        //on Home screen
        Intent shortcutIntent = new Intent(getApplicationContext(), SplashActivity.class);

        shortcutIntent.setAction(Intent.ACTION_MAIN);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.app_name));
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getApplicationContext(),R.drawable.ic_launcher));
        addIntent.putExtra("duplicate", false);
        AppController.getInstance().getPrefManger().setShortcutCreated(true);
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(SplashActivity.this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(SplashActivity.this).reportActivityStop(this);
    }
}