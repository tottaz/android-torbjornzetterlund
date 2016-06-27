package com.app.torbjornzetterlund.app;

public class Const {
    public static final String AuthenticationKey     = "YWU2YWFhMGM5OTZlZTdmODI2ZmZlMDk3ZGEwNGM2ODM0ZTU2YmNmNTdkN2RiNjE2Njk4YmY5M2EwZmI0ZWYyMA==";
//    public static final String URL_BLOG_CATEGORIES   = "http://www.torbjornzetterlund.com/wp2android-api/v1/categories/";
//    public static final String URL_RECENTLY_ADDED    = "http://www.torbjornzetterlund.com/wp2android-api/v1/latest/";
//    public static final String URL_RECENTLY_ADDED    = "http://www.torbjornzetterlund.com/api/get_recent_posts/";
//    public static final String URL_CATEGORY_POST	 = "http://www.torbjornzetterlund.com/wp2android-api/v1/category/_CAT_ID_/";
//    public static final String URL_STORY_PAGE	 	 = "http://www.torbjornzetterlund.com/wp2android-api/v1/post/_STORY_ID_/";
//    public static final String URL_SEARCH_RESULT	 = "http://www.torbjornzetterlund.com/wp2android-api/v1/search/_SEARCH_KEYWORD_/_PAGE_NO_";
//    public static final String URL_COMMENTS_PAGE	    = "http://www.torbjornzetterlund.com/wp2android-api/v1/post_comment/_STORY_ID_/";
//    public static final String URL_COMMENTS_LIST_PAGE   = "http://www.torbjornzetterlund.com/wp2android-api/v1/comments/_STORY_ID_/_PAGE_NO_";



    public static final String URL_BLOG_CATEGORIES   = "http://www.torbjornzetterlund.com/wp-json/wp/v2/categories?per_page=30";
    public static final String URL_RECENTLY_ADDED    = "http://www.torbjornzetterlund.com/wp-json/wp/v2/posts/";
    //    public static final String URL_RECENTLY_ADDED    = "http://www.torbjornzetterlund.com/api/get_recent_posts/";
    public static final String URL_CATEGORY_POST	 = "http://torbjornzetterlund.com/wp-json/wp/v2/posts?categories/_CAT_ID_/";
    public static final String URL_STORY_PAGE	 	 = "http://torbjornzetterlund.com/wp-json/wp/v2/posts/_STORY_ID_/";
    public static final String URL_SEARCH_RESULT	 = "http://www.torbjornzetterlund.com/wp-json/wp/v2/posts?search=_SEARCH_KEYWORD_&per_page=_PAGE_NO_";
    public static final String URL_COMMENTS_PAGE	    = "http://torbjornzetterlund.com/wp-json/wp/v2/comments?post_comment/_STORY_ID_/";
    public static final String URL_COMMENTS_LIST_PAGE   = "http://torbjornzetterlund.com/wp-json/wp/v2/comments?post/_STORY_ID_/_PAGE_NO_";

    public static final String PACKAGE_INTENT = "com.app.torbjornzetterlund.NEW_UPDATE";
    public static final boolean ADMOBService_ACTIVE         = true;
    public static final int UpdateCheckIn = (15*60) * 1000; // 15 minutes
    public static final Boolean ShowPostAsWebView = true;
    public static final Boolean ShowPostOnExternalBrowser = true;
    public static final boolean Analytics_ACTIVE         = true;

    public static final int NameValidationLimit = 10; // 10 Characters
    public static final int CommentsValidationLimit = 40; // 10 Characters

    public static final String Per_Page = "60";

    public static final Boolean forceRTL = false;
    public static final String  forceRTLLang = "ar";

    public static final String Facebook_URL        = "https://www.facebook.com/totta.zetterlund";
    public static final String Twitter_URL         = "http://twitter.com/mrfindmore";
    public static final String GooglePlus_URL      = "https://plus.google.com/u/0/+TorbjornZetterlund-totta";

    public static final String ADMOB_DEVICE_ID= "93725BCE6464386AFECB8D12724A9FB5"; //Enter the Test Device ID where you can test the ADMob Feature
}
