package com.app.torbjornzetterlund;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.torbjornzetterlund.R;
import com.app.torbjornzetterlund.app.Const;
import com.app.torbjornzetterlund.utils.AnalyticsUtil;
import com.app.torbjornzetterlund.utils.Utils;
import com.google.android.gms.analytics.GoogleAnalytics;


public class AboutActivity extends AppCompatActivity {
    private static final String TAG = AboutActivity.class.getSimpleName();

    @Override
    public void onResume() {
        super.onResume();
        //Get a Tracker (should auto-report)
        if (Const.Analytics_ACTIVE) {
            AnalyticsUtil.sendScreenName(this, TAG);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        //Forcing RTL Layout, If Supports and Enabled from Const file
        Utils.forceRTLIfSupported(this);
        //Lollipop Style
        Utils.setStatusBarcolor(getWindow(), getResources().getColor(R.color.primary_dark));
        if (Utils.isLollipop())
            findViewById(R.id.shadow_toolbar).setVisibility(View.GONE);

        //Setting Up Support Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.about_title));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        TextView versionTxt = (TextView) findViewById(R.id.versionTxt);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionTxt.setText(pInfo.versionName);
            getSupportActionBar().setSubtitle(getString(R.string.about_version_title) + " " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            //Best effort
        }

        //Trigger an Action to Buy the App
        // Button b = (Button) findViewById(R.id.btnBuy);
        // b.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        String url = "http://codecanyon.net/item/blogpress-an-android-app-for-your-wordpress/9205748?ref=stealthysam";
        //        Uri uriUrl = Uri.parse(url);
        //        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        //        startActivity(launchBrowser);
        //    }
        // });
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(AboutActivity.this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(AboutActivity.this).reportActivityStop(this);
    }
}