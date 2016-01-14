package com.app.torbjornzetterlund;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.torbjornzetterlund.R;
import com.app.torbjornzetterlund.app.Const;
import com.app.torbjornzetterlund.utils.AnalyticsUtil;
import com.app.torbjornzetterlund.utils.Utils;

public class SocialProfile extends Fragment {

    private ProgressBar pBar;
    private TextView pBarText;

    public SocialProfile() {
    }

    public static SocialProfile newInstance(String websiteURL) {
        SocialProfile f = new SocialProfile();
        Bundle args = new Bundle();
        args.putString("httpURL", websiteURL);
        f.setArguments(args);
        return f;
    }

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.social_profiles, container, false);

        //Get a Tracker (should auto-report)
        if (Const.Analytics_ACTIVE) {
            AnalyticsUtil.sendScreenName(this.getActivity(), "SocialProfile");
        }

        //Forcing RTL Layout, If Supports and Enabled from Const file
        Utils.forceRTLIfSupported(getActivity());

        WebView mWebView;
        String httpURL;

        // Get Web view
        mWebView = (WebView) rootView.findViewById(R.id.webView); //This is the id you gave to the WebView in the main.xml
        pBar = (ProgressBar) rootView.findViewById(R.id.pbLoader); //This is the id you gave to the WebView in the main.xml
        pBarText = (TextView) rootView.findViewById(R.id.pbLoaderText);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setSupportZoom(true);       //Zoom Control on web (You don't need this //if ROM supports Multi-Touch
        mWebView.getSettings().setBuiltInZoomControls(true); //Enable Multitouch if supported by ROM

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }
        });

        if (savedInstanceState == null) {
            if (getArguments().getString("httpURL") == null) {
                httpURL = null;
            } else {
                httpURL = getArguments().getString("httpURL");
            }
        } else {
            httpURL = (String) savedInstanceState.getSerializable("httpURL");
        }

        mWebView.loadUrl(httpURL);

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                //Make the bar disappear after URL is loaded, and changes string to Loading...
                pBarText.setText("Loading...   (" + (progress) + "%)");
                pBar.setProgress(progress * 100); //Make the bar disappear after URL is loaded


                // Return the app name after finish loading
                if (progress == 100) {
                    pBar.setVisibility(View.GONE);
                    pBarText.setVisibility(View.GONE);
                }
            }
        });
        return rootView;
    }
}
