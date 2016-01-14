package com.app.torbjornzetterlund.utils;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.app.torbjornzetterlund.fragments.PostCommentsTab;
import com.app.torbjornzetterlund.fragments.ViewCommentsTab;

public class ViewCommentPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewCommentPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewCommentPagerAdapter is created
    String post_id;
    Boolean user_can_comment;


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewCommentPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb, String post_id, Boolean user_can_comment) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;
        this.post_id = post_id;
        this.user_can_comment = user_can_comment;
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {

        if(position == 0) // if the position is 0 we are returning the First tab
        {
            ViewCommentsTab tab1 = new ViewCommentsTab();
            tab1.post_id = post_id;
            return tab1;
        }
        else             // As we are having 2 tabs if the position is now 0 it must be 1 so we are returning second tab
        {
            PostCommentsTab tab2 = new PostCommentsTab();
            tab2.post_id = post_id;
            tab2.user_can_comment = user_can_comment;
            return tab2;
        }
    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return NumbOfTabs;
    }
}