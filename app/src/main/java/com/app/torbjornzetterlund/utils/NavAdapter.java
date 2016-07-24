package com.app.torbjornzetterlund.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.torbjornzetterlund.MainActivity;
import com.app.torbjornzetterlund.R;

import java.util.ArrayList;

public class NavAdapter extends RecyclerView.Adapter<NavAdapter.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;
    public static OnItemClickListener mItemClickListener;
    public static View.OnClickListener clFacebook, clTwitter, clGooglePlus;
    private Callback mCallback;


    public NavAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems, Callback callback) {
        this.context = context;
        this.navDrawerItems = navDrawerItems;
        this.mCallback = callback;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        int Holderid;

        TextView title;
        ImageView appIcon;
        Button btnFacebook, btnTwitter, btnGooglePlus;
        TextView appName;
        TextView appVersionBuild;


        public ViewHolder(View itemView,int ViewType) {                     // Creating ViewHolder Constructor with View and viewType As a parameter
            super(itemView);
            if(ViewType == TYPE_ITEM) {
                itemView.setOnClickListener(this);
                title = (TextView) itemView.findViewById(R.id.title);       // Creating TextView object with the id of textView from item_row.xml
                Holderid = 1;                                               // setting holder id as 1 as the object being populated are of type item row
            }
            else{
                appName = (TextView) itemView.findViewById(R.id.appName);         // Creating Text View object from header.xml for name
                appVersionBuild = (TextView) itemView.findViewById(R.id.appVersionBuild);       // Creating Text View object from header.xml for email
                appIcon = (ImageView) itemView.findViewById(R.id.appIcon);// Creating Image view object from header.xml for profile pic
                btnFacebook = (Button) itemView.findViewById(R.id.facebook_btn);
                btnTwitter  = (Button) itemView.findViewById(R.id.twitter_btn);
                btnGooglePlus = (Button) itemView.findViewById(R.id.google_btn);
                Holderid = 0;                                                // Setting holder id = 0 as the object being populated are of type header view
            }
        }

        @Override
        public void onClick(View v) {
            if (NavAdapter.mItemClickListener != null) {
                int position = getAdapterPosition();//getPostion is depreciated
                NavDrawerItem item = getItem(position);
                NavAdapter.mItemClickListener.onItemClick(v, item, position);
            }
        }


        public NavDrawerItem getItem(int position) {
            if (position < 0 || MainActivity.navDrawerItems.size() <= position) return null;
            return MainActivity.navDrawerItems.get(position);
        }


    }



    //Below first we ovverride the method onCreateViewHolder which is called when the ViewHolder is
    //Created, In this method we inflate the item_row.xml layout if the viewType is Type_ITEM or else we inflate header.xml
    // if the viewType is TYPE_HEADER
    // and pass it to the view holder

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_list_item, parent, false); //Inflating the layout
            ViewHolder vhItem = new ViewHolder(v,viewType); //Creating ViewHolder and passing the object of type view
            return vhItem; // Returning the created object
        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_holder,parent,false); //Inflating the layout
            ViewHolder vhHeader = new ViewHolder(v,viewType); //Creating ViewHolder and passing the object of type view
            return vhHeader; //returning the object created
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(holder.Holderid ==1) {
            holder.title.setText(navDrawerItems.get(getCorrectPosition(position)).getTitle()); // Setting the Text with the array of our Titles
            boolean isSelected = (getCorrectPosition(position)) == mCallback.getSelectedPosition();
            holder.itemView.setSelected(isSelected?true:false);
        }
        else{
            holder.appIcon.setImageResource(R.drawable.drawer_icon);           // Similarly we set the resources for header view
            holder.appName.setText(context.getString(R.string.app_name));
            holder.btnFacebook.setOnClickListener(clFacebook);
            holder.btnTwitter.setOnClickListener(clTwitter);
            holder.btnGooglePlus.setOnClickListener(clGooglePlus);

            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                holder.appVersionBuild.setText("Version " + pInfo.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                //Best effort
            }
        }
    }

    // This method returns the number of items present in the list
    @Override
    public int getItemCount() {
        return navDrawerItems.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }
    public void setFacebookListener(View.OnClickListener listener) {
        clFacebook = listener;
    }
    public void setTwitterListener(View.OnClickListener listener) {
        clTwitter = listener;
    }
    public void setGooglePlusListener(View.OnClickListener listener) {
        clGooglePlus = listener;
    }


    // With the following method we check what type of view is being passed
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    public interface OnItemClickListener {
        public void onItemClick(View v, NavDrawerItem item, int position);
    }

    /**
     * Accounts for non Navigation Item rows and returns a position minus the correct offset
     *
     * @param position
     *
     * @return
     */
    public int getCorrectPosition(int position) {
        return position - 1;
    }


    public interface Callback {
        int getSelectedPosition();
    }
}