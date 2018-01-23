package com.gd.analytics;

class GDstatic {

    protected static final String TEST_GAME_API_URL = "http://test.game.api.gamedistribution.com/game/get";
    protected static final String GAME_API_URL = "https://game.api.gamedistribution.com/game/get";
    protected static final String TUNNL_TAG_URL = "https://ana.tunnl.com"; // /at?id={{game-id}}&pageurl={{bundle-name}}
    protected static final String version = "v1.0";
    protected static Boolean enable = false;
    protected static Boolean debug = false;
    protected static String serverId;
    protected static String regId;
    protected static String gameId;
    protected static boolean reqBannerEnabled = true;
    protected static boolean reqInterstitialEnabled = true;
    protected static String adUnit = "ca-app-pub-3940256099942544/4411468910"; // currently it is test.
    protected static String testInterUnitId = "ca-app-pub-3940256099942544/4411468910";
    protected static String testBannerUnitId = "ca-app-pub-3940256099942544/6300978111";
    protected static String cordovaAdxUnitId = "ca-mb-app-pub-5192618204358860/8119020012";
    protected static boolean testAds = false;
    protected static String affiliateId;
}
