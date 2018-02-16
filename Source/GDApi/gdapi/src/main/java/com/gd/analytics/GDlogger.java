package com.gd.analytics;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class GDlogger {

    static Activity mContext;
    static GDad gDad = new GDad();

    static boolean isCordovaPlugin = false;

    /**
     * Initialize Game Distribution Java API
     *
     * @param    gameId            Your game id
     * @param    regId        Your game reg id
     * @param    _mContext        Should be Activity to detect main activity
     */
    public static void init(String gameId, String regId, Activity _mContext) {

        mContext = _mContext;

        if(GDutils.isOnline(mContext)){

            if (GDstatic.enable) {
                GDutils.log("API is already Initialized.");
            } else {
                mContext = _mContext;

                String[] gameserver = regId.toLowerCase().split("-");
                GDstatic.serverId = gameserver[5];
                GDstatic.regId = gameserver[0] + "-" + gameserver[1] + "-" + gameserver[2] + "-" + gameserver[3] + "-" + gameserver[4];
                GDstatic.gameId = gameId;

                GDstatic.enable = true;

                GDbanner.init();
                GDutils.log("Game Distribution Android API Init");
            }

        }
        else{

            if(GDlogger.gDad.devListener != null)
                GDlogger.gDad.devListener.onAPINotReady("API cannot connect to internet. Please check the network connection.");

        }


    }

    public static void init(String gameId, String regId, Activity _mContext, boolean isCordovaPlugin) {
        if (isCordovaPlugin) {
            GDlogger.isCordovaPlugin = true;
        }

        init(gameId, regId, _mContext);

    }

    /**
     * GDlogger enables messages between GDApi and Server
     */
    public static void debug(Boolean enable) {
        GDstatic.debug = enable;
    }

    /**
     * GDlogger enables test ads
     */
    public static void enableTestAds() {
        GDstatic.testAds = true;
    }

    /**
     * GDlogger shows banner
     */
    public static void ShowBanner(String size, String alignment, String position) {

        if(GDutils.isOnline(mContext)){
            String args = "{isInterstitial:false,size:" + size + ",alignment:" + alignment + ",position:" + position + "}";

            if (GDstatic.enable) {
                GDbanner.ShowBanner(args);
            } else {
                Log.i("GDLogger", "GDApi is not initialized!");
            }
        }
        else{
            if(GDlogger.gDad.devListener != null)
                GDlogger.gDad.devListener.onBannerFailed("API cannot connect to internet. Please check the network connection.");
        }


    }

    public static void ShowBanner(Boolean isInterstitial) {

        if(GDutils.isOnline(mContext)){
            String args = "{isInterstitial:" + isInterstitial + "}";
            if (GDstatic.enable) {
                GDbanner.ShowBanner(args);
            } else {
                Log.i("GDLogger", "GDApi is not initialized!");
            }
        }
        else{
            if(GDlogger.gDad.devListener != null)
                GDlogger.gDad.devListener.onBannerFailed("API cannot connect to internet. Please check the network connection.");
        }

    }

    public static void setAdListener(GDadListener gDadListener) {
        if (gDad != null)
            gDad.setAdListener(gDadListener);
    }

    /**
     * GDlogger hides banner
     */
    public static void hideBanner() {
        if (GDstatic.enable) {
            //	GDbanner.hide();
        }
    }


}
