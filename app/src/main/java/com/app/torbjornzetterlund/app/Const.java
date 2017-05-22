package com.app.torbjornzetterlund.app;

public class Const {

    public static final String URL_BLOG_CATEGORIES      = "https://torbjornzetterlund.com/wp-json/wp/v2/categories?per_page=60";
    public static final String URL_RECENTLY_ADDED       = "https://torbjornzetterlund.com/wp-json/wp/v2/posts/";
    public static final String URL_AUTHOR               = "https://torbjornzetterlund.com/wp-json/wp/v2/users/";//    public static final String URL_RECENTLY_ADDED    = "http://www.torbjornzetterlund.com/api/get_recent_posts/";
    public static final String URL_CATEGORY_POST	    = "https://torbjornzetterlund.com/wp-json/wp/v2/posts?categories=_CAT_ID_";
    public static final String URL_STORY_PAGE	 	    = "https://torbjornzetterlund.com/wp-json/wp/v2/posts/_STORY_ID_/";
    public static final String URL_SEARCH_RESULT	    = "https://torbjornzetterlund.com/wp-json/wp/v2/posts?search=_SEARCH_KEYWORD_";
    public static final String URL_COMMENTS_LIST_PAGE   = "https://torbjornzetterlund.com/wp-json/wp/v2/comments?post=_STORY_ID_";

    public static final String URL_REGISTER_DEVICE      = "https://torbjornzetterlund.com/wp-json/thorfcmapi/v2/register";

    public static final boolean ADMOBService_ACTIVE         = true;
    public static final Boolean ShowPostAsWebView = true;
    public static final Boolean ShowPostOnExternalBrowser = true;
    public static final boolean Analytics_ACTIVE         = true;

    public static final int NameValidationLimit = 10; // 10 Characters
    public static final int CommentsValidationLimit = 40; // 10 Characters

    public static final String Per_Page = "60";

    public static final Boolean forceRTL = false;
    public static final String  forceRTLLang = "ar";

    public static final String Facebook_URL        = "https://www.facebook.com/totta.zetterlund";
    public static final String Twitter_URL         = "https://twitter.com/mrfindmore";
    public static final String GooglePlus_URL      = "https://plus.google.com/u/0/+TorbjornZetterlund-totta";

    //Enter the Test Device ID where you can test the ADMob Feature
    public static final String ADMOB_DEVICE_ID= "93725BCE6464386AFECB8D12724A9FB5";

}
