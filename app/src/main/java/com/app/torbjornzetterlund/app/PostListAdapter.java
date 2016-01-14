package com.app.torbjornzetterlund.app;

import com.app.torbjornzetterlund.R;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.squareup.picasso.Picasso;


public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Post> postItems = new ArrayList<Post>();
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    Boolean isAdLoaded = false;
    private int lastPosition = -1;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView category;
        TextView timestamp;
        TextView description;
        ImageView postImageView;
        LinearLayout ll;
        Context context;

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public ViewHolder(View itemView,int ViewType,Context c) {                 // Creating ViewHolder Constructor with View and viewType As a parameter
            super(itemView);
            context = c;
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            name = (TextView) itemView.findViewById(R.id.name);
            category = (TextView) itemView.findViewById(R.id.category);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            postImageView = (ImageView) itemView.findViewById(R.id.feedImage1);
            ll = (LinearLayout) itemView.findViewById(R.id.rtlView);
            if (Const.forceRTL) {
                ll.setLayoutDirection(LinearLayout.LAYOUT_DIRECTION_RTL);
            }
            String postFormat = AppController.getInstance().getPrefManger().getPostDisplayFormat();
            switch (postFormat) {
                case "large":
                    description = (TextView) itemView.findViewById(R.id.txtDescription);
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            //Toast.makeText(context, "The Item Clicked is: " + getPosition(), Toast.LENGTH_SHORT).show();
        }
    }

    public PostListAdapter(Activity activity, List<Post> postItems) {
        this.activity = activity;
        this.postItems = postItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null; //Inflating the layout
        String postFormat = AppController.getInstance().getPrefManger().getPostDisplayFormat();
        switch (postFormat) {
            case "large":
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_item_large, parent, false);
                break;
            case "small":
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_item, parent, false);
                break;
        }
        ViewHolder vhItem = new ViewHolder(v, viewType, activity); //Creating ViewHolder and passing the object of type view
        return vhItem; // Returning the created object
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // position by 1 and pass it to the holder while setting the text and image

        Post item = postItems.get(position);
        holder.name.setText(Html.fromHtml(item.getName()));
        holder.category.setText(Html.fromHtml(item.getCategory()));

        String postFormat = AppController.getInstance().getPrefManger().getPostDisplayFormat();
        switch (postFormat){
            case "large":
                Typeface font = Typeface.createFromAsset(activity.getAssets(), "fonts/GenR102.ttf");
                holder.name.setTypeface(font);
                holder.name.setTextSize(24);
                if (item.getDescription().length()>0) {
                    holder.description.setText(Html.fromHtml(item.getDescription()));
                }else {
                    holder.description.setVisibility(View.GONE);
                }
                break;
        }

        // Converting timestamp into x ago format

        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                Long.parseLong(item.getTimeStamp()),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        holder.timestamp.setText(timeAgo);

        // Feed image
        if (item.getImge() != null) {
            Picasso.with(this.activity).load(item.getImge()).into(holder.postImageView);


            /*holder.postImageView.setImageUrl(item.getImge(), imageLoader);
            holder.postImageView.setVisibility(View.VISIBLE);
            holder.postImageView
                    .setResponseObserver(new PostImageView.ResponseObserver() {
                        @Override
                        public void onError() {
                        }

                        @Override
                        public void onSuccess() {
                        }
                    });
            */
        } else {
            holder.postImageView.setVisibility(View.GONE);
        }
    }

    // This method returns the number of items present in the list
    @Override
    public int getItemCount() {
        return postItems.size();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

}