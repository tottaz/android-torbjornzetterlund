package com.app.torbjornzetterlund;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.app.torbjornzetterlund.app.AppController;
import com.app.torbjornzetterlund.app.Const;
import com.app.torbjornzetterlund.app.Post;
import com.app.torbjornzetterlund.app.TransitionAdapter;
import com.app.torbjornzetterlund.utils.AnalyticsUtil;
import com.app.torbjornzetterlund.utils.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class PostViewActivity extends AppCompatActivity {

    private static final String TAG = PostViewActivity.class.getSimpleName();
    public static final String TAG_SEL_POST_ID = "post_id";
    public static final String TAG_SEL_POST_TITLE = "post_title";
    public static final String TAG_SEL_POST_IMAGE = "post_image";
    private String commentsNumber;


    private String selectedPostID, selectedPostTitle;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private ImageButton fab;
    private TextView post_name;
    private TextView post_content;
    private TextView post_author;
    private ImageView postImageView;
    private String post_image, objURL, objTitle;
    private Integer commentsCount;
    private TextView timestamp;
    private NetworkImageView profilePic;
    private ShareActionProvider mShareActionProvider;
    private WebView post_contentHTML;
    private Boolean user_can_comment = true;

    private Spanned spannedContent;
    private ProgressBar pbLoader;
    private LinearLayout llayout;

    private Integer OC = 0;

    //----------Exit and Banner Ad----------------------------------
    private InterstitialAd interstitial;
    private AdView mAdView;

    private Toolbar toolbar;

    public static void navigate(AppCompatActivity activity, View transitionImage, Post post) {
        Intent intent = new Intent(activity, PostViewActivity.class);
        intent.putExtra(TAG_SEL_POST_IMAGE, post.getImge());
        intent.putExtra(TAG_SEL_POST_TITLE, post.getName());

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionImage, TAG_SEL_POST_IMAGE);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString(TAG_SEL_POST_ID, selectedPostID);
        savedInstanceState.putString(TAG_SEL_POST_TITLE, selectedPostTitle);
        Log.d(TAG, savedInstanceState.toString());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "Restored: " + savedInstanceState.toString());
        selectedPostID = (String) savedInstanceState.getString(TAG_SEL_POST_ID);
        selectedPostTitle = (String) savedInstanceState.getString(TAG_SEL_POST_TITLE);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Get a Tracker (should auto-report)
        if (Const.Analytics_ACTIVE) {
            AnalyticsUtil.sendScreenName(this, TAG);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initActivityTransitions();
        setContentView(R.layout.activity_post_view);

        //Forcing RTL Layout, If Supports and Enabled from Const file
        Utils.forceRTLIfSupported(this);

        ActivityCompat.postponeEnterTransition(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //Get and Set the Post Title
        String itemTitle = getIntent().getStringExtra(TAG_SEL_POST_TITLE);
        setTitle(Html.fromHtml(itemTitle));

        if (savedInstanceState == null) {
            if (getIntent().getExtras() == null) {
                selectedPostID = null;
                selectedPostTitle = "Unknown Error";
            } else {
                selectedPostID = getIntent().getExtras().getString(TAG_SEL_POST_ID);
                selectedPostTitle = getIntent().getExtras().getString(TAG_SEL_POST_TITLE);
            }
        } else {
            Log.d(TAG, "Resume: " + savedInstanceState.toString());
            selectedPostID = (String) savedInstanceState.getString(TAG_SEL_POST_ID);
            selectedPostTitle = (String) savedInstanceState.getString(TAG_SEL_POST_TITLE);
            /*selectedPostID = (String) savedInstanceState.getSerializable(TAG_SEL_POST_ID);
            selectedPostTitle = (String) savedInstanceState.getSerializable(TAG_SEL_POST_TITLE);*/

        }

        //Setting up Fields Display
        fab = (ImageButton) findViewById(R.id.fab);
        post_name = (TextView) findViewById(R.id.title);
        timestamp = (TextView) findViewById(R.id.timestamp);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/GenR102.ttf");
        post_name.setTypeface(font);


        float density = getResources().getDisplayMetrics().density;
        int leftPadding = (int) (20 * density);
        Configuration config = getResources().getConfiguration();
        if (ViewCompat.getLayoutDirection(post_name) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            //post_name.setPadding(leftPadding,R.dimen.spacing_large,0,0);
            post_name.setPadding(leftPadding, 0, 0, 0);
        } else {
            post_name.setPadding(0, 0, leftPadding, 0);
        }


        post_content = (TextView) findViewById(R.id.description);
        post_contentHTML = (WebView) findViewById(R.id.descriptionHTML);
        if (!Const.ShowPostAsWebView) {
            post_content.setMovementMethod(LinkMovementMethod.getInstance());
            Typeface font_postcontent = Typeface.createFromAsset(getAssets(), "fonts/OSRegular.ttf");
            post_content.setTypeface(font_postcontent);
        } else {
            post_contentHTML.setVisibility(View.VISIBLE);
            WebSettings webSettings = post_contentHTML.getSettings();
            post_contentHTML.getSettings().setJavaScriptEnabled(true);
            post_contentHTML.addJavascriptInterface(this, "BlogPress");
            post_contentHTML.getSettings().setAllowContentAccess(true);
            post_contentHTML.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            post_contentHTML.getSettings().setLoadsImagesAutomatically(true);
            post_contentHTML.getSettings().setDefaultTextEncodingName("utf-8");
            post_contentHTML.getSettings().setUseWideViewPort(true);
            post_contentHTML.getSettings().setLoadWithOverviewMode(true);
            post_contentHTML.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            post_contentHTML.setWebChromeClient(new WebChromeClient());
        }

        post_author = (TextView) findViewById(R.id.post_author);


        postImageView = (ImageView) findViewById(R.id.image);
        profilePic = (NetworkImageView) findViewById(R.id.profilePic);


        //Setting up Post Image and Toolbar Activity
        //final ImageView image = (ImageView) findViewById(R.id.image);
        /*ViewCompat.setTransitionName(postImageView, TAG_SEL_POST_IMAGE);

        Bitmap bitmap = ((BitmapDrawable) postImageView.getDrawable()).getBitmap();
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                applyPalette(palette, postImageView);
            }
        });*/


        //Setting Up Post Admob Banner
        ////Standard Banner
        mAdView = (AdView) findViewById(R.id.adView);
        if (Const.ADMOBService_ACTIVE) {
            //----------Exit Ad----------------------------------
            interstitial = new InterstitialAd(this);
            interstitial.setAdUnitId(getString(R.string.unit_id_interstitial));
            AdRequest adRequestInterstitial = new AdRequest.Builder().addTestDevice(Const.ADMOB_DEVICE_ID).build();
            interstitial.loadAd(adRequestInterstitial);
            //----------Exit Ad----------------------------------

            AdRequest adRequest = new AdRequest.Builder().addTestDevice(Const.ADMOB_DEVICE_ID).build();
            mAdView.loadAd(adRequest);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            mAdView.setVisibility(View.GONE);
        }

        getPost();

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initActivityTransitions() {
        if (Utils.isLollipop()) {
            Slide transition = new Slide();
            transition.excludeTarget(android.R.id.statusBarBackground, true);
            getWindow().setEnterTransition(transition);
            getWindow().setReturnTransition(transition);
        }
    }

    private void applyPalette(Palette palette, ImageView image) {
        int primaryDark = getResources().getColor(R.color.primary_dark);
        int primary = getResources().getColor(R.color.primary);
        toolbar.setBackgroundColor(primary);
        Utils.setStatusBarcolor(getWindow(), primaryDark);
        initScrollFade(image);
        ActivityCompat.startPostponedEnterTransition(this);
    }

    private void initScrollFade(final ImageView image) {
        final View scrollView = findViewById(R.id.scroll);

        setComponentsStatus(scrollView, image);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                setComponentsStatus(scrollView, image);
            }
        });
    }

    private void setComponentsStatus(View scrollView, ImageView image) {
        int scrollY = scrollView.getScrollY();
        image.setTranslationY(-scrollY / 2);
        ColorDrawable background = (ColorDrawable) toolbar.getBackground();
        int padding = scrollView.getPaddingTop();
        double alpha = (1 - (((double) padding - (double) scrollY) / (double) padding)) * 255.0;
        alpha = alpha < 0 ? 0 : alpha;
        alpha = alpha > 255 ? 255 : alpha;

        background.setAlpha((int) alpha);

        float scrollRatio = (float) (alpha / 255f);
        int titleColor = getAlphaColor(Color.WHITE, scrollRatio);
        toolbar.setTitleTextColor(titleColor);
        toolbar.setSubtitleTextColor(titleColor);
    }

    private int getAlphaColor(int color, float scrollRatio) {
        return Color.argb((int) (scrollRatio * 255f), Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * It seems that the ActionBar view is reused between activities. Changes need to be reverted,
     * or the ActionBar will be transparent when we go back to Main Activity
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void restablishActionBar() {
        if (Utils.isLollipop()) {
            getWindow().getReturnTransition().addListener(new TransitionAdapter() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    toolbar.setTitleTextColor(Color.WHITE);
                    toolbar.setSubtitleTextColor(Color.WHITE);
                    toolbar.getBackground().setAlpha(255);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        restablishActionBar();
        if (Const.ADMOBService_ACTIVE) {
            displayInterstitial();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            restablishActionBar();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getPost() {
        //Requesting The Story
        String url = null;
        url = Const.URL_STORY_PAGE.replace("_STORY_ID_", selectedPostID.replace("P", ""));
        //Log.i(TAG, "Taging: " + url);
        // making fresh volley request and getting json
        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //Log.i(TAG, "Taging: " + response.toString());
                VolleyLog.d(TAG, "Response: " + response.toString());
                if (response != null) {
                    try {
                        if (response.has("error")) {
                            String error = response.getString("error");
                            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                        } else {
                            parseJsonFeed(response);
                        }
                    } catch (JSONException es) {
                        es.printStackTrace();
                        Toast.makeText(getApplicationContext(), es.getMessage(), Toast.LENGTH_LONG).show();
                    }
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

    private void getAuthorByID(String id){
        //Requesting The Story
        String url = null;
        url = Const.URL_AUTHOR + id;
        //Log.i(TAG, "Taging: " + url);
        // making fresh volley request and getting json
        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //Log.i(TAG, "Taging: " + response.toString());
                VolleyLog.d(TAG, "Response: " + response.toString());
                if (response != null) {
                    try {
                        post_author.setText(response.getString("name"));
                    } catch (JSONException es) {
                        es.printStackTrace();
                        Toast.makeText(getApplicationContext(), es.getMessage(), Toast.LENGTH_LONG).show();
                    }
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

    private void parseJsonFeed(JSONObject feedObj) {
        try {
            objTitle = feedObj.getJSONObject("title").getString("rendered");
            post_name.setText(Html.fromHtml(objTitle));

            commentsCount = feedObj.getInt("comments_count");

            if (!feedObj.getString("comment_status").equals("open")) {
                user_can_comment = false;
            }

            if (!Const.ShowPostAsWebView) {
                post_contentHTML.setVisibility(View.GONE);
                URLImageParser p = new URLImageParser(this, post_content);
                spannedContent = Html.fromHtml(feedObj.getJSONObject("content").getString("rendered"), p, null);
                post_content.setText(trimTrailingWhitespace(spannedContent));
            } else {

                post_content.setVisibility(View.GONE);
                post_contentHTML.setVisibility(View.VISIBLE);

                String post_con = "<!DOCTYPE html>" +
                        "<html lang=\"en\">" +
                        "  <head>" +
                        "    <meta charset=\"utf-8\">" +
                        "  </head>" +
                        "  <body>" +
                        "    #content# " +
                        "  </body>" +
                        "</html>";
                try {
                    InputStream in_s = getResources().openRawResource(R.raw.post_format);
                    byte[] b = new byte[in_s.available()];
                    in_s.read(b);
                    post_con = new String(b);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                post_con = post_con.replace("#title#", feedObj.getJSONObject("title").getString("rendered"));
                post_con = post_con.replace("#content#", feedObj.getJSONObject("content").getString("rendered"));
                //post_contentHTML.loadData(post_con, "text/html; charset=utf-8", "utf-8");
                post_contentHTML.loadDataWithBaseURL(null,
                        post_con,
                        "text/html",
                        "UTF-8",
                        null);
            }

            if (feedObj.getJSONObject("content").getString("rendered").length() <= 0) {
                post_content.setVisibility(View.GONE);
                post_contentHTML.setVisibility(View.GONE);
            }
            this.getAuthorByID(feedObj.getString("author"));

            //my getSupportActionBar().setSubtitle("By " + feedObj.getString("author"));

            post_image = feedObj.getString("featured_image_big_url");
            objURL = feedObj.getString("link");

            if (Const.Analytics_ACTIVE) {
                AnalyticsUtil.sendEvent(this, "Post View", objTitle, objURL);
            }

            //Setting Up a Share Intent
            fab.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, objTitle + " - " + objURL);
                    startActivityForResult(Intent.createChooser(shareIntent, "Share via"), 300);
                }
            });

            //setShareIntent(shareIntent);

            //Comment Button Click
            Button viewComments = (Button) findViewById(R.id.btnViewComments);
            commentsNumber = Utils.formatNumber(commentsCount);
            viewComments.setText(String.format(getString(R.string.comments_button), commentsNumber));
            viewComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent();
                    i.setClass(PostViewActivity.this, PostComments.class);
                    i.putExtra("post_id", selectedPostID.replace("P", ""));
                    i.putExtra("post_title", getIntent().getStringExtra(TAG_SEL_POST_TITLE));
                    i.putExtra("commentsCount", commentsCount);
                    i.putExtra("user_can_comment", user_can_comment);
                    startActivityForResult(i, 1000);
                }
            });

            //Button Click
            Button viewWeb = (Button) findViewById(R.id.btnViewWeb);
            if (Const.ShowPostOnExternalBrowser) {
                viewWeb.setVisibility(View.VISIBLE);
                viewWeb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToUrl(objURL);
                    }
                });
            }

            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date date = formatter.parse(feedObj.getString("date"));
                // Converting timestamp into x ago format
                CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                        Long.parseLong(String.valueOf(date.getTime())),
                        System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
                timestamp.setText(timeAgo);
            }catch (ParseException e){

            }


            profilePic.setImageUrl(feedObj.getString("author_image_thumbnail_url"), imageLoader);


            loadConfig();
            //pbLoader.setVisibility(View.GONE);
            //llayout.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void goToUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    private void loadConfig() {
        if (post_image != null) {
            ViewCompat.setTransitionName(postImageView, TAG_SEL_POST_IMAGE);
            Picasso.with(this).load(post_image).into(postImageView, new Callback() {
                @Override
                public void onSuccess() {
                    Bitmap bitmap = ((BitmapDrawable) postImageView.getDrawable()).getBitmap();
                    new Palette.Builder(bitmap).generate(new Palette.PaletteAsyncListener() {
                        public void onGenerated(Palette palette) {
                            applyPalette(palette, postImageView);
                        }
                    });
                    /*Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                        public void onGenerated(Palette palette) {
                            applyPalette(palette, postImageView);
                        }
                    });*/
                }

                @Override
                public void onError() {

                }
            });
        } else {
            postImageView.setVisibility(View.GONE);
        }
    }


    @JavascriptInterface
    public void resize(final float height) {
        PostViewActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Trigger: " + height + " // " + (int) (height * getResources().getDisplayMetrics().density), Toast.LENGTH_LONG).show();
                //post_contentHTML.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) (height * getResources().getDisplayMetrics().density)));
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        post_contentHTML.loadUrl("javascript:BlogPress.resize(document.body.getBoundingClientRect().height)");
    }

    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if (source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while (--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i + 1);
    }

    // Invoke displayInterstitial() when you are ready to display an interstitial.
    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }


    public class URLImageParser implements Html.ImageGetter {
        @SuppressWarnings("deprecation")
        class URLDrawable extends BitmapDrawable {
            // the drawable that you need to set, you could set the initial drawing
            // with the loading image if you need to
            protected Drawable drawable;

            @Override
            public void draw(Canvas canvas) {
                // override the draw to facilitate refresh function later
                if (drawable != null) {
                    drawable.draw(canvas);
                }
            }
        }

        class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
            URLDrawable urlDrawable;

            public ImageGetterAsyncTask(URLDrawable d) {
                this.urlDrawable = d;
            }

            @Override
            protected Drawable doInBackground(String... params) {
                String source = params[0];
                Uri uri = Uri.parse(source);
                Bitmap bitmap = null;
                try {
                    Picasso pic = new Picasso.Builder(mContext).build();
                    bitmap = pic.load(uri).get();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                return new BitmapDrawable(mContext.getResources(), bitmap);
            }

            @Override
            protected void onPostExecute(Drawable result) {
                // set the correct bound according to the result from HTTP call
                urlDrawable.setBounds(0, 0, 0 + result.getIntrinsicWidth(), 0
                        + result.getIntrinsicHeight());

                // change the reference of the current drawable to the result
                // from the HTTP call
                urlDrawable.drawable = result;

                // redraw the image by invalidating the container
                URLImageParser.this.mView.invalidate();
            }
        }

        private final Context mContext;
        private final View mView;

        public URLImageParser(Context context, View view) {
            mContext = context;
            mView = view;
        }

        @Override
        public Drawable getDrawable(String source) {
            Uri uri = Uri.parse(source);
            URLDrawable urlDrawable = new URLDrawable();
            ImageGetterAsyncTask task = new ImageGetterAsyncTask(urlDrawable);
            task.execute(source);
            return urlDrawable;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(PostViewActivity.this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(PostViewActivity.this).reportActivityStop(this);
    }
}
