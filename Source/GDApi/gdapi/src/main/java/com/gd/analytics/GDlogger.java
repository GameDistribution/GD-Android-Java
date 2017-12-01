package com.gd.analytics;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


public class GDlogger {

    static Activity mContext;
    static SharedPreferences appSharedPrefs;
    static GDad gDad;

    /**
     * Initialize Game Distribution Java API
     *
     * @param    gameId            Your game id
     * @param    regId        Your game reg id
     * @param    _mContext        Should be Activity to detect main activity
     */

    public static void init(String gameId, String regId, Activity _mContext) {

        mContext = _mContext;

        if (!checkPlayServices(_mContext)) {
            GDutils.log("Google play services out of date. Update it in order to use GDApi properly.");
        }

        if (GDstatic.enable) {
            GDutils.log("API is already Initialized.");
        } else {
            mContext = _mContext;
            gDad = new GDad();
            gDad.init(GDlogger.mContext);

            appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());

            String[] gameserver = regId.toLowerCase().split("-");
            GDstatic.serverId = gameserver[5];
            GDstatic.regId = gameserver[0] + "-" + gameserver[1] + "-" + gameserver[2] + "-" + gameserver[3] + "-" + gameserver[4];
            GDstatic.gameId = gameId;

            GDstatic.enable = true;

            GDbanner.init();
            GDutils.log("Game Distribution Android API Init");
        }
    }

    public static void init(String gameId, String regId, Activity _mContext, boolean isCordovaPlugin) {

        if (isCordovaPlugin) {
            mContext = _mContext;

            if (!checkPlayServices(_mContext)) {
                GDutils.log("Google play services out of date. Update it in order to use GDApi properly.");
            }

            if (GDstatic.enable) {
                GDutils.log("API is already Initilized.");
            } else {
                mContext = _mContext;
                gDad = new GDad();
                gDad.init(GDlogger.mContext, true);

                appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());

                String[] gameserver = regId.toLowerCase().split("-");
                GDstatic.serverId = gameserver[5];
                GDstatic.regId = gameserver[0] + "-" + gameserver[1] + "-" + gameserver[2] + "-" + gameserver[3] + "-" + gameserver[4];
                GDstatic.gameId = gameId;
                GDstatic.enable = true;

                GDbanner.init();
                GDutils.log("Game Distribution Android API Init");
            }
        } else {
            init(gameId, regId, _mContext);
        }


    }

    /**
     * GDlogger enables messages between GDApi and Server
     */
    public static void debug(Boolean enable) {
        GDstatic.debug = enable;
    }

    private static int incPlay() {
        int play = GDutils.getCookie("play");
        play++;
        GDutils.setCookie("play", play);
        return play;
    }

    /**
     * GDlogger shows banner
     */
    public static void ShowBanner(String size, String alignment, String position) {

        String args = "{isInterstitial:false,size:" + size + ",alignment:" + alignment + ",position:" + position + "}";

        if (GDstatic.enable) {
            GDbanner.ShowBanner(args);
        } else {
            Log.i("GDLogger", "GDApi is not initialized!");
        }
    }

    public static void ShowBanner(Boolean isInterstitial) {

        String args = "{isInterstitial:" + isInterstitial + "}";
        if (GDstatic.enable) {
            GDbanner.ShowBanner(args);
        } else {
            Log.i("GDLogger", "GDApi is not initialized!");
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

    public static boolean checkPlayServices(Activity activity) {
        try {
            final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            int result = googleAPI.isGooglePlayServicesAvailable(activity);
            if (result != ConnectionResult.SUCCESS) {
                if (googleAPI.isUserResolvableError(result)) {
                    googleAPI.getErrorDialog(activity, result,
                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
                }
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

}
