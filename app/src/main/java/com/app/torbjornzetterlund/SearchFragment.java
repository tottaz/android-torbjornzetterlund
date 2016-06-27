package com.app.torbjornzetterlund;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
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
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.app.torbjornzetterlund.app.AppController;
import com.app.torbjornzetterlund.app.Const;
import com.app.torbjornzetterlund.app.Post;
import com.app.torbjornzetterlund.app.PostListAdapter;
import com.app.torbjornzetterlund.utils.AnalyticsUtil;
import com.app.torbjornzetterlund.utils.ConnectionDetector;
import com.app.torbjornzetterlund.utils.RecyclerItemClickListener;
import com.app.torbjornzetterlund.utils.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment {
    private static final String TAG = SearchFragment.class.getSimpleName();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView listView;
    private RecyclerView.Adapter listAdapter;
    private LinearLayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager

    private List<Post> feedItems;
    private static final String bundleSearchQuery = "searchQuery";
    private String selectedQuery;
    private ProgressBar pbLoader;
    private TextView pbNoInternet;
    private TextView pbNoResult;
    private int pageNum, numOfPages;
    private boolean isLoadingProgress = true;

    View footerView;

    private AdView mAdView;

    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    public SearchFragment() {
    }

    public static SearchFragment newInstance(String searchQuery) {
        SearchFragment f = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(bundleSearchQuery, searchQuery);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(com.app.torbjornzetterlund.R.layout.fragment_grid, container, false);

        //Get a Tracker (should auto-report)
        if (Const.Analytics_ACTIVE) {
            AnalyticsUtil.sendScreenName(this.getActivity(), TAG);
        }

        //Forcing RTL Layout, If Supports and Enabled from Const file
        Utils.forceRTLIfSupported(getActivity());


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
        feedItems = new ArrayList<Post>();
        listAdapter = new PostListAdapter(getActivity(), feedItems);
        listView.setAdapter(listAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity());                 // Creating a layout Manager

        listView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager


        if (getArguments().getString(bundleSearchQuery) != null) {
            selectedQuery = getArguments().getString(bundleSearchQuery);
        } else {
            selectedQuery = null;
        }

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

        RecyclerItemClickListener.OnItemClickListener itemClickListener = new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Intent i = new Intent();
                i.setClass(getActivity(), PostViewActivity.class);

                Post post = feedItems.get(position);
                i.putExtra(PostViewActivity.TAG_SEL_POST_ID, "P" + post.getId());
                i.putExtra(PostViewActivity.TAG_SEL_POST_TITLE, post.getName());
                getActivity().startActivityForResult(i, 500);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        };
        listView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), itemClickListener));

        /*listView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(),motionEvent.getY());
                if(child!=null && mGestureDetector.onTouchEvent(motionEvent)){
                    // On selecting the grid image, we launch fullscreen activity
                    Intent i = new Intent();
                    i.setClass(getActivity(), PostViewActivity.class);
                    // Passing selected image to fullscreen activity
                    Post post = feedItems.get(recyclerView.getChildPosition(child));
                    i.putExtra(PostViewActivity.TAG_SEL_POST_ID, "P" + post.getId());
                    i.putExtra(PostViewActivity.TAG_SEL_POST_TITLE, post.getName());
                    getActivity().startActivityForResult(i, 500);
                    return true;

                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

            }
        });*/



        //On Scroll Event
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (isLoadingProgress == false){

                    if(pageNum < numOfPages) {
                        //load more data
                        pageNum++;
                        loadSearchData(pageNum);
                    }else{
                        //listView.removeFooterView(footerView);
                        return;
                    }
                }
            }
        });
        /*listView.setOnScrollListener(new PostRVOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {

                Toast.makeText(getActivity(), "Current Page: " + pageNum +"<"+ numOfPages, Toast.LENGTH_LONG).show();

                if(pageNum < numOfPages) {
                    //load more data
                    pageNum++;
                    loadSearchData(pageNum);
                }else{
                    //listView.removeFooterView(footerView);
                    return;
                }
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                final int lastItem = firstVisibleItem + visibleItemCount;
                if (isLoadingProgress == false){

                    if(pageNum < numOfPages) {
                        //load more data
                        pageNum++;
                        loadSearchData(pageNum);
                    }else{
                        //listView.removeFooterView(footerView);
                        return;
                    }
                }
            }
        });*/

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

        url = Const.URL_SEARCH_RESULT.replace("_SEARCH_KEYWORD_", selectedQuery);
        url = url.replace("_PAGE_NO_", ""+pageNum);
        Toast.makeText(getActivity(), url, Toast.LENGTH_LONG);

        // Build and Send the Analytics Event.
        if (Const.Analytics_ACTIVE) {
            AnalyticsUtil.sendEvent(this.getActivity(), "Search", selectedQuery, selectedQuery);
        }

        // making fresh volley request and getting json
        JsonObjectRequest jsonReq = new JsonObjectRequest(Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "Response: " + response.toString());
                if (response != null) {
                    isLoadingProgress=false;
                    try {
                        if (response.has("error")) {
                            String error = response.getString("error");
                            Toast.makeText(getActivity().getApplicationContext(), error, Toast.LENGTH_LONG).show();
                        }else {

                            if (response.isNull("feed")) {
                                pbLoader.setVisibility(View.GONE);
                                listView.setVisibility(View.GONE);
                                pbNoResult.setVisibility(View.VISIBLE);
                            }else{
                                numOfPages = response.getInt("total_pages");
                                parseJsonFeed(response);
                            }
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }catch (JSONException es) {
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

                Post item = new Post();
                item.setId(feedObj.getInt("id"));
                Log.d(TAG, "ID: " + feedObj.getInt("id"));
                item.setName(feedObj.getString("name"));
                item.setCategory(feedObj.getString("category"));

                // Image might be null sometimes
                String image = feedObj.isNull("image") ? null : feedObj
                        .getString("image");
                item.setImge(image);
                //item.setStatus(feedObj.getString("status"));
                item.setProfilePic(feedObj.getString("profilePic"));
                item.setTimeStamp(feedObj.getString("timeStamp"));
                item.setDescription(feedObj.getString("description"));

                // url might be null sometimes
                String feedUrl = feedObj.isNull("url") ? null : feedObj.getString("url");
                item.setUrl(feedUrl);

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