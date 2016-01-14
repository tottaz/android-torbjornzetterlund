package com.app.torbjornzetterlund;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;

import com.app.torbjornzetterlund.R;
import com.app.torbjornzetterlund.app.Const;
import com.app.torbjornzetterlund.utils.AnalyticsUtil;
import com.app.torbjornzetterlund.utils.Utils;
import com.app.torbjornzetterlund.utils.ViewCommentPagerAdapter;
import com.app.torbjornzetterlund.views.SlidingTabLayout;

public class PostComments extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    // Declaring Your View and Variables

    Toolbar toolbar;
    ViewPager pager;
    ViewCommentPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"Comments (0)","Post a Comment"};
    int Numboftabs =2;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        //Get a Tracker (should auto-report)
        if (Const.Analytics_ACTIVE) {
            AnalyticsUtil.sendScreenName(this, TAG);
        }

        //Forcing RTL Layout, If Supports and Enabled from Const file
        Utils.forceRTLIfSupported(this);


        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (Utils.isLollipop()) {
            toolbar.setElevation(0.0f);
        }
        /*toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });*/
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(Html.fromHtml(getIntent().getStringExtra("post_title")));

        String form = Utils.formatNumber(getIntent().getIntExtra("commentsCount", 0));
        Titles[0] = String.format(getString(R.string.comments_tab), form);
        Titles[1] = getString(R.string.comment_tab_post);

        getSupportActionBar().setSubtitle(String.format(getString(R.string.comments_button), form));


        // Creating The ViewCommentPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter =  new ViewCommentPagerAdapter(getSupportFragmentManager(),Titles,Numboftabs, getIntent().getStringExtra("post_id"), getIntent().getBooleanExtra("user_can_comment",true));

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.primary_dark);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == android.R.id.home)
        {
            Intent intent = NavUtils.getParentActivityIntent(this);
            // printing this intent, it shows to have flags but no extras
            NavUtils.navigateUpFromSameTask(this); // tried finish() here but that created an even bigger mess
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
