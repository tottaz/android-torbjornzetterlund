package com.app.torbjornzetterlund.app;

import com.app.torbjornzetterlund.R;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.squareup.picasso.Picasso;


public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Comment> postItems = new ArrayList<Comment>();
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    Boolean isAdLoaded = false;
    private int lastPosition = -1;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView author;
        TextView timestamp;
        TextView content;
        ImageView profilePic;
        Context context;

        public ViewHolder(View itemView,int ViewType,Context c) {                 // Creating ViewHolder Constructor with View and viewType As a parameter
            super(itemView);
            context = c;
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            author = (TextView) itemView.findViewById(R.id.author);
            content = (TextView) itemView.findViewById(R.id.content);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            profilePic = (ImageView) itemView.findViewById(R.id.profilePic);
        }

        @Override
        public void onClick(View v) {
            //Toast.makeText(context, "The Item Clicked is: " + getPosition(), Toast.LENGTH_SHORT).show();
        }
    }

    public CommentListAdapter(Activity activity, List<Comment> postItems) {
        this.activity = activity;
        this.postItems = postItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.commentview_item, parent, false);
        ViewHolder vhItem = new ViewHolder(v, viewType, activity); //Creating ViewHolder and passing the object of type view
        return vhItem; // Returning the created object
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // position by 1 and pass it to the holder while setting the text and image

        Comment item = postItems.get(position);
        holder.author.setText(Html.fromHtml(item.getAuthor()));
        holder.content.setText(Html.fromHtml(item.getContent()));

        // Converting timestamp into x ago format
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                Long.parseLong(item.getTimeStamp()),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        holder.timestamp.setText(timeAgo);

        // Feed image
        if (item.getProfilePic() != null) {
            Picasso.with(this.activity).load(item.getProfilePic()).into(holder.profilePic);
        } else {
            holder.profilePic.setVisibility(View.GONE);
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