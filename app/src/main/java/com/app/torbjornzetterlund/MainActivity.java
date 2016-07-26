package com.app.torbjornzetterlund;

//import android.app.Activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.app.torbjornzetterlund.Services.BroadcastService;
import com.app.torbjornzetterlund.app.AppController;
import com.app.torbjornzetterlund.app.Category;
import com.app.torbjornzetterlund.app.Const;
import com.app.torbjornzetterlund.utils.AnalyticsUtil;
import com.app.torbjornzetterlund.utils.NavAdapter;
import com.app.torbjornzetterlund.utils.NavDrawerItem;
import com.app.torbjornzetterlund.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavAdapter.Callback,
                                                                NavAdapter.OnItemClickListener {
    //public static int currentPosition = 0;

    private static final String TAG = MainActivity.class.getSimpleName();
    private DrawerLayout mDrawerLayout;
    //private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private SearchView searchView;

    // Navigation drawer title
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private List<Category> categoriesList;
    public static ArrayList<NavDrawerItem> navDrawerItems;
    //private NavDrawerListAdapter adapter;
    private String CurrentOpen = "";
    private int mCurrentSelectedPosition = 0;

    /////
    private RecyclerView mRecyclerView;                           // Declaring RecyclerView
    private NavAdapter mAdapter;                        // Declaring Adapter For Recycler View
    private RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    private DrawerLayout Drawer;                                  // Declaring DrawerLayout

    @Override
    public void onResume() {
        super.onResume();
        //Get a Tracker (should auto-report)
        if (Const.Analytics_ACTIVE) {
            AnalyticsUtil.sendScreenName(this, TAG);
        }

    }

    //private Callbacks mCallbacks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Forcing RTL Layout, If Supports and Enabled from Const file
        Utils.forceRTLIfSupported(this);

        //Lollipop Style
        Utils.setStatusBarcolor(getWindow(), getResources().getColor(R.color.primary_dark));
        if (Utils.isLollipop()) {
            findViewById(R.id.shadow_toolbar).setVisibility(View.GONE);
        }

        //if (AppController.getInstance().getPrefManger().getFirstLaunched()){
            //Starting the Service for Push/Status Notification
            startService();
        //}

        //Registering Broadcast Receiver
        registerReceiver(this.myBroadCast, new IntentFilter(Const.PACKAGE_INTENT));

        // Initialize Parse
        // Parse.initialize(this, getString(R.string.parse_application_id), getString(R.string.parse_client_key));
        // get current installation
        // ParseInstallation.getCurrentInstallation().saveInBackground();
        // Send Analytics to Parse
        // ParseAnalytics.trackAppOpenedInBackground(getIntent());

        //Get a Tracker (should auto-report)
        /*if (Const.Analytics_ACTIVE) {
            AnalyticsUtil.sendScreenName(this, TAG);
        }*/

        //Setting Up Support Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Listing All Fetched Categories
        navDrawerItems = new ArrayList<NavDrawerItem>();
        // Getting the albums from shared preferences
        categoriesList = AppController.getInstance().getPrefManger().getCategories();
        // Insert "Recently Added" in navigation drawer first position
        Category recentCategory = new Category(null,  getString(R.string.recently_added));
        categoriesList.add(0, recentCategory);
        // Loop through cache and add them to navigation drawer adapter
        for (Category a : categoriesList) {
            navDrawerItems.add(new NavDrawerItem(a.getId(), a.getTitle()));
        }


        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View
        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size
        mAdapter = new NavAdapter(getApplicationContext(), navDrawerItems, this);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        mAdapter.setFacebookListener(new OnClickListener(){
            @Override
            public void onClick(View arg0) {
                displaySocialProfile (getString(R.string.follow_facebook), Const.Facebook_URL);
            }
        });
        mAdapter.setTwitterListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                displaySocialProfile(getString(R.string.follow_twitter), Const.Twitter_URL);
            }
        });
        mAdapter.setGooglePlusListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                displaySocialProfile(getString(R.string.follow_googleplus), Const.GooglePlus_URL);
            }
        });



        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);                              // Setting the adapter to RecyclerView
        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager

        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager


        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this,Drawer,toolbar,R.string.app_name,R.string.app_name){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I don't want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }



        }; // Drawer Toggle Object Made
        Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State


        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                setSubtitle("Search Results for: " + query);
                searchView.onActionViewCollapsed();
                searchView.setQuery("", false);
                searchView.clearFocus();
                menu.findItem(R.id.action_search).collapseActionView();

                CurrentOpen = "Search";
                Fragment fragment = SearchFragment.newInstance(query);
                if (fragment != null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
                } else {
                    // error in creating fragment
                    Toast.makeText(getApplicationContext(), "Error in creating search fragment", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (item.getItemId() == R.id.menu_settings) {
            Intent settingsActivityIntent = new Intent();
            settingsActivityIntent.setClass(this, SettingsActivity.class);
            this.startActivityForResult(settingsActivityIntent, 111);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        unregisterReceiver(this.myBroadCast);
        super.onDestroy();
    }

    public void onBPress(){
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        final Activity thisActivity= this;
        new AlertDialog.Builder(this, R.style.AlertDialogCustom_Destructive)
            .setTitle(R.string.exit_title)
            .setMessage(R.string.exit_message)
            .setNegativeButton(android.R.string.no, null)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.finishAffinity(thisActivity);
                }
            }).create().show();
    }

    public int getSelectedPosition() {
        return mCurrentSelectedPosition;
    }


    /**
     * Displaying fragment view for selected nav drawer list item
     * */
    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                // Recently added item selected
                // don't pass album id to home fragment
                CurrentOpen = "Recent";
                fragment = GridFragment.newInstance(null, getString(R.string.recently_added));
                break;

            default:
                // selected wallpaper category
                // send album id to home fragment to list all the wallpapers
                CurrentOpen = "Other";
                String categoryId = categoriesList.get(position).getId();
                String categoryName = categoriesList.get(position).getTitle();
                fragment = GridFragment.newInstance(categoryId, categoryName);
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).commit();

            // update selected item and title, then close the drawer
            //mDrawerList.setItemChecked(position, true);
            //mDrawerList.setSelection(position);
            setSubtitle(categoriesList.get(position).getTitle());
            //mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // error in creating fragment
            Toast.makeText(getApplicationContext(),"Error in creating fragment", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }
    /**
     * Displaying fragment view for social profiles
     * */
    private void displaySocialProfile(CharSequence title, String url) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        fragment = SocialProfile.newInstance(url);

        if (fragment != null) {
            Drawer.closeDrawers();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
            setSubtitle(title);
        } else {
            // error in creating fragment
            Toast.makeText(getApplicationContext(),"Error in creating social fragment", Toast.LENGTH_LONG).show();
        }
    }

    private  void setSubtitle (CharSequence title) {
        //mTitle = title;
        getSupportActionBar().setSubtitle(title);
    }

    @Override
    public void onItemClick(View v, NavDrawerItem item, int position) {
        selectItem(mAdapter.getCorrectPosition(position));
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        Drawer.closeDrawers();
        displayView(position);
        mAdapter.notifyDataSetChanged();
    }



    /////Initializing Service
    private void startService() {
        Intent start = new Intent(MainActivity.this, BroadcastService.class);
        if(startService(start)!=null){
            startService(start);
        }
    }

    final private BroadcastReceiver myBroadCast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (action.equals(Const.PACKAGE_INTENT)) {
                if (CurrentOpen=="Recent") {
                    displayView(0);
                }
            }
        }
    };


}
