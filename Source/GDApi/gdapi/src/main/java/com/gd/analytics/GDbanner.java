package com.gd.analytics;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

class GDbanner {

    private static Thread adBannerTimer, adInterstitialTimer;

    protected static void init() {

        if(GDstatic.enable) {
            GDHttpRequest.sendHttpRequest(GDlogger.mContext, GDstatic.GAME_API_URL, Request.Method.GET, null, new GDHttpCallback() {
                @Override
                public void onSuccess(JSONObject data) {

                    try {
                        boolean success = data.getBoolean("success");
                        if(success) {
                            JSONObject result = data.getJSONObject("result");
                            JSONObject game = result.getJSONObject("game");

                            GDGameData.enableAds = game.getBoolean("enableAds");
                            GDGameData.gameMd5 = game.getString("gameMd5");
                            GDGameData.preRoll = game.getBoolean("preRoll");
                            GDGameData.timeAds = game.getInt("timeAds");
                            GDGameData.title = game.getString("title");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        GDutils.log("Something went wrong parsing json game data.\nData:\n"+data.toString());
                    }

                }

                @Override
                public void onError(VolleyError error) {
                    GDutils.log("Something went wrong fetching json game data.");
                }
            });
        }
    }

    protected static void ShowBanner(String args) {

        Gson gson = new Gson();
        final GDshowObj gDshowObj;
        gDshowObj = gson.fromJson(args, GDshowObj.class);


        if (GDGameData.enableAds && gDshowObj._key != null && gDshowObj._key.equals("preroll") && GDlogger.gDad != null) {

            if (GDGameData.preRoll) {
                GDlogger.gDad.setmUnitId(GDstatic.adUnit);
                GDlogger.gDad.showBanner(args);
            } else {
                if (GDlogger.gDad.devListener != null) {
                    GDlogger.gDad.devListener.onBannerFailed("Banner request failed: 'Preroll is disabled.'");
                }
            }
        } else { // so this is for midroll request

            if (GDGameData.enableAds && GDGameData.timeAds != 0 && GDlogger.gDad != null) {

                if (gDshowObj.isInterstitial) {
                    if (GDstatic.reqInterstitialEnabled) {
                        GDlogger.gDad.setmUnitId(GDstatic.adUnit);
                        GDlogger.gDad.showBanner(args);
                        adInterstitialTimer = null;
                        setAdTimer(true); // inter timer
                        GDstatic.reqInterstitialEnabled = false;
                    } else {
                        GDutils.log("You can not invoke 'ShowBanner()' within " + GDGameData.timeAds + " min(s).");
                    }
                } else {
                    if (GDstatic.reqBannerEnabled) {
                        GDlogger.gDad.setmUnitId(GDstatic.adUnit);
                        GDlogger.gDad.showBanner(args);
                        adBannerTimer = null;
                        setAdTimer(false); // banner timer
                        GDstatic.reqBannerEnabled = false;
                    } else {
                        GDutils.log("You can not invoke 'ShowBanner()' within " + GDGameData.timeAds + " min(s).");
                    }
                }
            } else {
                if (GDlogger.gDad.devListener != null) {
                    GDlogger.gDad.devListener.onBannerFailed("Banner request failed: 'Midroll is disabled.'");
                }
            }

        }


    }

    private static void setAdTimer(final boolean isInterstitial) {

        if (GDstatic.enable) {

            if (isInterstitial) {
                adInterstitialTimer = new Thread(new Runnable() {
                    public void run() {
                        while (true) {
                            try {
                                Thread.sleep(GDGameData.timeAds * 60000);
                                if (!GDutils.isApplicationBroughtToBackground()) {
                                    adTimerHandler(isInterstitial);
                                }
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                });
                adInterstitialTimer.start();
            } else {
                adBannerTimer = new Thread(new Runnable() {
                    public void run() {
                        while (true) {
                            try {
                                Thread.sleep(GDGameData.timeAds * 60000);
                                if (!GDutils.isApplicationBroughtToBackground()) {
                                    adTimerHandler(isInterstitial);
                                }
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                });
                adBannerTimer.start();
            }


        }
    }

    private static void adTimerHandler(boolean isInterstitial) {
        if (isInterstitial) {
            GDstatic.reqInterstitialEnabled = true;
        } else {
            GDstatic.reqBannerEnabled = true;
        }
    }
}
