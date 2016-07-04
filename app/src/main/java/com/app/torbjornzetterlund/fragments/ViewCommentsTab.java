package com.app.torbjornzetterlund.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.app.torbjornzetterlund.R;
import com.app.torbjornzetterlund.app.AppController;
import com.app.torbjornzetterlund.app.Comment;
import com.app.torbjornzetterlund.app.CommentListAdapter;
import com.app.torbjornzetterlund.app.Const;
import com.app.torbjornzetterlund.utils.AnalyticsUtil;
import com.app.torbjornzetterlund.utils.ConnectionDetector;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.JsonParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ViewCommentsTab extends Fragment {
    private static final String TAG = ViewCommentsTab.class.getSimpleName();
    public String post_id;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView listView;
    private RecyclerView.Adapter listAdapter;
    private LinearLayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager

    private List<Comment> feedItems;
    private ProgressBar pbLoader;
    private TextView pbNoInternet;
    private TextView pbNoResult;
    private int pageNum, numOfPages;
    private boolean isLoadingProgress = true;

    private AdView mAdView;

    Boolean isInternetPresent = false;
    ConnectionDetector cd;
    private String next_url = "";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView =inflater.inflate(R.layout.tab_viewcomments,container,false);
        listView = (RecyclerView) rootView.findViewById(R.id.list_view);
        listView.setVisibility(View.GONE);
        pbLoader = (ProgressBar) rootView.findViewById(R.id.pbLoader);
        pbLoader.setVisibility(View.VISIBLE);
        pbNoInternet = (TextView) rootView.findViewById(R.id.pbNoInternet);
        pbNoInternet.setVisibility(View.GONE);
        pbNoResult   = (TextView) rootView.findViewById(R.id.pbNoResult);
        pbNoResult.setVisibility(View.GONE);

        pageNum = numOfPages = 1;

        if (Const.ADMOBService_ACTIVE) {
            mAdView = (AdView) rootView.findViewById(R.id.adView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice(Const.ADMOB_DEVICE_ID).build();
            mAdView.loadAd(adRequest);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                }
            });
        }

        //Feed Items
        feedItems = new ArrayList<Comment>();
        listAdapter = new CommentListAdapter(getActivity(), feedItems);
        listView.setAdapter(listAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity());                 // Creating a layout Manager

        listView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager

        checkInternetConnection();



        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                //refreshItems();
                feedItems.clear();
                listAdapter.notifyDataSetChanged();
                pageNum = numOfPages = 1;
                checkInternetConnection();
            }
        });

        final GestureDetector mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {

            @Override public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

        });



        //On Scroll Event
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if(dy > 0) //check for scroll down
                {
                    if (isLoadingProgress == false) {
                        if ((visibleItemCount + firstVisibleItem) >= totalItemCount) {
                            if (next_url != "") {
                                //load more data
                                loadSearchData(pageNum);
                            } else {
                                //listView.removeFooterView(footerView);
                                return;
                            }
                        }
                    }
                }
            }
        });
        return rootView;
    }

    public void checkInternetConnection (){
        // creating connection detector class instance
        cd = new ConnectionDetector(this.getActivity());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            loadSearchData(pageNum);
        } else {
            // Hide the loader, make grid visible
            pbLoader.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            pbNoInternet.setVisibility(View.VISIBLE);
        }
    }

    public void loadSearchData(int pageNum){
        String url = null;
        isLoadingProgress = true;

        if(this.next_url == "") {
            url = Const.URL_COMMENTS_LIST_PAGE.replace("_STORY_ID_", post_id);
            url = url.replace("_PAGE_NO_", ""+pageNum);
        }else{
            url = this.next_url;
        }

        Toast.makeText(getActivity(), url, Toast.LENGTH_LONG);

        // Build and Send the Analytics Event.
        if (Const.Analytics_ACTIVE) {
            AnalyticsUtil.sendEvent(this.getActivity(), "View Comments", post_id, "");
        }

        // making fresh volley request and getting json
        JsonArrayRequest jsonReq = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                //VolleyLog.d(TAG, "Response: " + response.toString());
                if (response.length() != 0) {
                    isLoadingProgress=false;
                    try {
                        //numOfPages = response.getInt("total_pages");
                        Log.d(TAG, response.toString());
                        parseJsonArrayFeed(response);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }catch (JsonParseException es) {
                        es.printStackTrace();
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                    }
                }else{
                    pbLoader.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                    pbNoResult.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        }) {
            /** Passing some request headers **/
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
//                headers.put("ApiKey", Const.AuthenticationKey);
                return headers;
            }

            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));

                    if(response.headers.get("Link") != null && response.headers.get("Link") != "") {
                        String links = response.headers.get("Link");
                        int next_idx = links.lastIndexOf("next");
                        if(next_idx > 0) {
                            next_url = links.substring(links.lastIndexOf("<") + 1, links.lastIndexOf(">"));
                        }else{
                            next_url = "";
                        }
                    }

                    return Response.success(new JSONArray(jsonString),
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };

        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(jsonReq);

    }
    /**
     * Parsing json reponse and passing the data to feed view list adapter
     * */
    private void parseJsonFeed(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("feed");

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                Comment item = new Comment();
                item.setId(feedObj.getInt("ID"));
                Log.d(TAG, "ID: " + feedObj.getInt("ID"));
                item.setAuthor(feedObj.getString("author"));
                item.setAuthorEmail(feedObj.getString("author_email"));

                //item.setStatus(feedObj.getString("status"));
                String image = feedObj.isNull("avatar") ? null : feedObj.getString("avatar");
                item.setProfilePic(image);
                item.setTimeStamp(feedObj.getString("comment_date"));
                item.setContent(feedObj.getString("comment_content"));
                feedItems.add(item);
            }

            // notify data changes to list adapater
            listAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);

            // Hide the loader, make grid visible
            pbLoader.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void parseJsonArrayFeed(JSONArray response) {
        try {
            JSONArray feedArray = response;

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                Comment item = new Comment();
                item.setId(feedObj.getInt("id"));
                Log.d(TAG, "ID: " + feedObj.getInt("id"));
                item.setAuthor(feedObj.getString("author_name"));
                //item.setAuthorEmail(feedObj.getString("author_email"));

                //item.setStatus(feedObj.getString("status"));
                String image = feedObj.getJSONObject("author_avatar_urls").getString("96") == null ? null : feedObj.getJSONObject("author_avatar_urls").getString("96");
                item.setProfilePic(image);

                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                    Date date = formatter.parse(feedObj.getString("date"));
                    item.setTimeStamp(String.valueOf(date.getTime()));
                }catch (ParseException e){

                }

                item.setContent(feedObj.getJSONObject("content").getString("rendered"));
                feedItems.add(item);
            }

            // notify data changes to list adapater
            listAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);

            // Hide the loader, make grid visible
            pbLoader.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
