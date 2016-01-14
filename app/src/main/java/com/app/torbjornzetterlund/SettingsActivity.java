package com.app.torbjornzetterlund;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.app.torbjornzetterlund.R;
import com.app.torbjornzetterlund.app.AppController;
import com.app.torbjornzetterlund.app.Const;
import com.app.torbjornzetterlund.utils.AnalyticsUtil;
import com.app.torbjornzetterlund.utils.Utils;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Get a Tracker (should auto-report)
        if (Const.Analytics_ACTIVE) {
            AnalyticsUtil.sendScreenName(this, TAG);
        }

        //Forcing RTL Layout, If Supports and Enabled from Const file
        Utils.forceRTLIfSupported(this);

        //Lollipop Style
        Utils.setStatusBarcolor(getWindow(), getResources().getColor(R.color.primary_dark));
        if (Utils.isLollipop())
            findViewById(R.id.shadow_toolbar).setVisibility(View.GONE);

        //Setting Up Support Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Setting up Up Navigation Button
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment {
        Preference  about;
        ListPreference post_display_format;
        CheckBoxPreference notify_me;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.activity_settings);

            addPreferencesAction();
        }

        public void addPreferencesAction(){
            /////////////////Status Notification Preference
            notify_me = (CheckBoxPreference) findPreference("statusNotifications");
            notify_me.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.equals(true)) {
                        AppController.getInstance().getPrefManger().notificationStatus(true);
                    } else {
                        AppController.getInstance().getPrefManger().notificationStatus(false);
                    }
                    return true;
                }
            });

            /////////////////Post Style Preference
            post_display_format = (ListPreference) findPreference("post_display_format");
            post_display_format.setValue(AppController.getInstance().getPrefManger().getPostDisplayFormat());
            post_display_format.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    AppController.getInstance().getPrefManger().setPostDisplayFormat(newValue.toString());
                    return true;
                }
            });

            /////////////////About Preference
            about = (Preference) findPreference("about_app");
            about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    Intent AboutActivityIntent = new Intent();
                    AboutActivityIntent.setClass(getActivity(), AboutActivity.class);
                    getActivity().startActivityForResult(AboutActivityIntent, 111);

                    return false;
                }
            });
        }

    }
}