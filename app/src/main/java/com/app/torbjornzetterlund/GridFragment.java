package com.app.torbjornzetterlund;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.app.torbjornzetterlund.app.AppController;
import com.app.torbjornzetterlund.app.Category;
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
import com.google.gson.JsonParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GridFragment extends Fragment {
	private static final String TAG = GridFragment.class.getSimpleName();

    private SwipeRefreshLayout mSwipeRefreshLayout;
	private RecyclerView listView;
	private RecyclerView.Adapter listAdapter;
    //private RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    private List<Post> feedItems;
	private static final String bundleCategoryId = "categoryId";
    private static final String bundleCategoryName = "categoryName";
	private String selectedCategoryId, selectedCategoryName;
	private ProgressBar pbLoader;
	private TextView pbNoInternet;
    private TextView pbNoResult;
    private List<Category> categoriesList = AppController.getInstance().getPrefManger().getCategories();

    private AdView mAdView;

	
	public GridFragment() {
	}

	public static GridFragment newInstance(String categoryId, String categoryName) {
		GridFragment f = new GridFragment();
		Bundle args = new Bundle();
		args.putString(bundleCategoryId, categoryId);
        args.putString(bundleCategoryName, categoryName);
        f.setArguments(args);
		return f;
	}

    @Override
    public void onResume() {
        super.onResume();
        //Get a Tracker (should auto-report)
        if (Const.Analytics_ACTIVE) {
            AnalyticsUtil.sendScreenName(getActivity(), TAG);
        }

    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(com.app.torbjornzetterlund.R.layout.fragment_grid, container, false);

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


        listView.setItemAnimator(new DefaultItemAnimator());

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
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());                 // Creating a layout Manager
        if(Const.forceRTL==Boolean.TRUE) {
            mLayoutManager.setReverseLayout(true);
            mLayoutManager.setStackFromEnd(true);
        }
        listView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager
		

		if (getArguments().getString(bundleCategoryId) != null) {
			selectedCategoryId = getArguments().getString(bundleCategoryId);
		} else {
			selectedCategoryId = null;
		}

        if (getArguments().getString(bundleCategoryName) != null) {
            selectedCategoryName = getArguments().getString(bundleCategoryName);
        } else {
            selectedCategoryName = null;
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
                checkInternetConnection();
            }
        });

        /*final GestureDetector mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });*/

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

		return rootView;
	}

    private void checkInternetConnection (){
		// creating connection detector class instance
        ConnectionDetector cd = new ConnectionDetector(this.getActivity());
        Boolean isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
        	loadCategoriesData();
        } else {
        	// Hide the loader, make grid visible
			pbLoader.setVisibility(View.GONE);
			listView.setVisibility(View.GONE);
			pbNoInternet.setVisibility(View.VISIBLE);
        }
	}

    private void loadCategoriesData(){
		String url;

		if (selectedCategoryId == null) {
			url = Const.URL_RECENTLY_ADDED;
            // Build and Send the Analytics Event.
            if (Const.Analytics_ACTIVE) {
                AnalyticsUtil.sendEvent(this.getActivity(), getString(R.string.recently_added), "View", "");
            }
		} else {
			url = Const.URL_CATEGORY_POST.replace("_CAT_ID_", selectedCategoryId);
            // Build and Send the Analytics Event.
            if (Const.Analytics_ACTIVE) {
                AnalyticsUtil.sendEvent(this.getActivity(), selectedCategoryName, "View", "");
            }
		}

        //Toast.makeText(getActivity(),url,Toast.LENGTH_LONG).show();

        // making fresh volley request and getting json
        JsonArrayRequest jsonReq = new JsonArrayRequest(Method.GET, url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                if (response != null) {
                    try {
                        if (response.length() == 0) {
                            pbLoader.setVisibility(View.GONE);
                            listView.setVisibility(View.GONE);
                            pbNoResult.setVisibility(View.VISIBLE);
                        }else{
                            parseJsonArrayFeed(response);
                        }
                        mSwipeRefreshLayout.setRefreshing(false);

                        /*if (response.has("error")) {
                            String error = response.getString("error");
                            Toast.makeText(getActivity().getApplicationContext(), error, Toast.LENGTH_LONG).show();
                        }else {
                            if (response.isNull("feed")) {
                                pbLoader.setVisibility(View.GONE);
                                listView.setVisibility(View.GONE);
                                pbNoResult.setVisibility(View.VISIBLE);
                            }else{
                                parseJsonFeed(response);
                            }
                            mSwipeRefreshLayout.setRefreshing(false);
                        }*/

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
        };

        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(jsonReq);

		// Grid item select listener
		/*listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				// On selecting the grid image, we launch fullscreen activity
				Intent i = new Intent();
                i.setClass(getActivity(), PostViewActivity.class);
				// Passing selected image to fullscreen activity
				Post photo = feedItems.get(position);
				i.putExtra(PostViewActivity.TAG_SEL_POST_ID, "P"+photo.getId());
				i.putExtra(PostViewActivity.TAG_SEL_POST_TITLE, photo.getName());
				getActivity().startActivityForResult(i, 500);
			}
		});*/
	}
	/**
	 * Parsing json response and passing the data to feed view list adapter
	 * */
	private void parseJsonFeed(JSONObject response) {
		try {
			JSONArray feedArray = response.getJSONArray("feed");

			for (int i = 0; i < feedArray.length(); i++) {
				JSONObject feedObj = (JSONObject) feedArray.get(i);

				Post item = new Post();
				item.setId(feedObj.getInt("id"));
				item.setName(feedObj.getString("name"));
                item.setCategory(feedObj.getString("category"));

				// Image might be null sometimes
                String image = null;
                String postFormat = AppController.getInstance().getPrefManger().getPostDisplayFormat();
                switch (postFormat){
                    case "large":
                        image = feedObj.isNull("image_big") ? null : feedObj.getString("image_big");
                        break;
                    case "small":
                        image = feedObj.isNull("image") ? null : feedObj.getString("image");
                        break;
                }
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

			// notify data changes to list adapter
			listAdapter.notifyDataSetChanged();
			
			
			// Hide the loader, make grid visible
			pbLoader.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
    /**
	 * Parsing json response and passing the data to feed view list adapter
	 * */
	private void parseJsonArrayFeed(JSONArray response) {
		try {
			JSONArray feedArray = response;

			for (int i = 0; i < feedArray.length(); i++) {
				JSONObject feedObj = (JSONObject) feedArray.get(i);

				Post item = new Post();

				item.setId(feedObj.getInt("id"));
				item.setName(feedObj.getJSONObject("title").getString("rendered"));

                for(int j =0; j < categoriesList.size(); j++){
                    if(categoriesList.get(j).getId().equals(feedObj.getJSONArray("categories").get(0).toString())){
                        item.setCategory(categoriesList.get(j).getTitle());
                        break;
                    }
                }




				// Image might be null sometimes
                String image = null;
                String postFormat = AppController.getInstance().getPrefManger().getPostDisplayFormat();
                switch (postFormat){
                    case "large":
                        image = feedObj.isNull("featured_image_big_url") ? null : feedObj.getString("featured_image_big_url");
                        break;
                    case "small":
                        image = feedObj.isNull("featured_image_thumbnail_url") ? null : feedObj.getString("featured_image_thumbnail_url");
                        break;
                }
				item.setImge(image);
				//item.setStatus(feedObj.getString("status"));


                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                    Date date = formatter.parse(feedObj.getString("date"));
                    item.setTimeStamp(String.valueOf(date.getTime()));
                }catch (ParseException e){

                }

                item.setProfilePic(feedObj.getString("author_image_thumbnail_url"));

                item.setDescription(feedObj.getJSONObject("excerpt").getString("rendered"));

				// url might be null sometimes
				String feedUrl = feedObj.isNull("link") ? null : feedObj.getString("link");
				item.setUrl(feedUrl);

				feedItems.add(item);
			}

			// notify data changes to list adapter
			listAdapter.notifyDataSetChanged();


			// Hide the loader, make grid visible
			pbLoader.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}