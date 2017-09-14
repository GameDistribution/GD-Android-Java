package com.gd.analytics;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
     * @param	gameId			Your game id
     * @param	regId		Your game reg id
     * @param	_mContext		Should be Activity to detect main activity
     */

    public static void init(String gameId, String regId, Activity _mContext) {

        mContext = _mContext;

        if(!checkPlayServices(_mContext)){
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
            GDstatic.regId = gameserver[0]+"-"+gameserver[1]+"-"+gameserver[2]+"-"+gameserver[3]+"-"+gameserver[4];
            GDstatic.gameId = gameId;

            GDlogchannel.postObj.gid = gameId;
            GDlogchannel.postObj.ref = "http://Android.os";
            GDlogchannel.postObj.sid = GDutils.sessionId();
            GDlogchannel.postObj.ver = GDstatic.version;

            GDstatic.enable = true;

            GDlogger.visit();

            GDlogchannel.init();
            GDbanner.init();
            GDutils.log("Game Distribution Android API Init");
        }
    }

    public static void init(String gameId, String regId, Activity _mContext,boolean isCordovaPlugin) {

        if(isCordovaPlugin){
            mContext = _mContext;

            if(!checkPlayServices(_mContext)){
                GDutils.log("Google play services out of date. Update it in order to use GDApi properly.");
            }

            if (GDstatic.enable) {
                GDutils.log("API is already Initilized.");
            } else {
                mContext = _mContext;
                gDad = new GDad();
                gDad.init(GDlogger.mContext,true);

                appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());

                String[] gameserver = regId.toLowerCase().split("-");
                GDstatic.serverId = gameserver[5];
                GDstatic.regId = gameserver[0]+"-"+gameserver[1]+"-"+gameserver[2]+"-"+gameserver[3]+"-"+gameserver[4];
                GDstatic.gameId = gameId;

                GDlogchannel.postObj.gid = gameId;
                GDlogchannel.postObj.ref = "http://Android.os";
                GDlogchannel.postObj.sid = GDutils.sessionId();
                GDlogchannel.postObj.ver = GDstatic.version;

                GDstatic.enable = true;

                GDlogger.visit();

                GDlogchannel.init();
                GDbanner.init();
                GDutils.log("Game Distribution Android API Init");
            }
        }
        else{
            init(gameId,regId,_mContext);
        }


    }

    /**
     * GDlogger enables messages between GDApi and Server
     */
    public static void debug(Boolean enable) {
        GDstatic.debug = enable;
    }

    /**
     * GDlogger First visit to server
     */
    public static void visit() {
        if (GDstatic.enable) {
            GDsendObj sendObj = new GDsendObj();
            sendObj.action = "visit";
            sendObj.value = incVisit();
            sendObj.state = GDutils.getCookie("state");
            GDlogrequest.pushLog(sendObj);
        }
    }

    protected static int incVisit() {
        int visit = GDutils.getCookie("visit");
        visit++;
        GDutils.setCookie("visit", visit);
        return visit;
    }

    /**
     * GDlogger sends how many times 'Play' is called. If you invoke 'Play' many times, it increases 'Play' counter and sends this counter value.
     */
    public static void play() {
        if (GDstatic.enable) {
            GDsendObj sendObj = new GDsendObj();
            sendObj.action = "play";
            sendObj.value = incPlay();
            GDlogrequest.pushLog(sendObj);
        }
    }

    /**
     * GDlogger sends how many times 'CustomLog' that is called related to given by _key name. If you invoke 'CustomLog' many times, it increases 'CustomLog' counter and sends this counter value.
     */
    public static void customlog(String _key) {
        if (GDstatic.enable) {
            if (_key != "play" || _key != "visit") {
                int customValue = GDutils.getCookie(_key);
                if (customValue == 0) {
                    customValue = 1;
                    GDutils.setCookie(_key, customValue);
                }

                GDsendObj sendObj = new GDsendObj();
                sendObj.action = "custom";
                sendObj.value = new GDcustomLog(_key, customValue);
                GDlogrequest.pushLog(sendObj);
            }
        }
    }

    private static int incPlay() {
        int play = GDutils.getCookie("play");
        play++;
        GDutils.setCookie("play", play);
        return play;
    }

    protected static GDsendObj ping() {
        if (GDstatic.enable) {
            GDsendObj sendObj = new GDsendObj();
            sendObj.action = "ping";
            sendObj.value = "ping";
            return sendObj;
        }
        return null;
    }
    /**
     * GDlogger shows banner
     */
    public static void ShowBanner(String size, String alignment, String position) {

        String args = "{isInterstitial:false,size:"+size+",alignment:"+alignment+",position:"+position+"}";

        if (GDstatic.enable) {
            GDbanner.ShowBanner(args);
        }else{
            Log.i("GDLogger","GDApi is not initialized!");
        }
    }
    public static void ShowBanner(Boolean isInterstitial){

        String args = "{isInterstitial:"+isInterstitial+"}";
        if (GDstatic.enable) {
            GDbanner.ShowBanner(args);
        }else{
            Log.i("GDLogger","GDApi is not initialized!");
        }
    }

    public  static void setAdListener(GDadListener gDadListener){
        if(gDad != null)
            gDad.setAdListener(gDadListener);
    }

    public  static void addTestDevice(String deviceID){
        if(gDad != null)
            gDad.setDeviceID(deviceID);
    }

    public static String  getTestDevice(){
        if(gDad != null)
            return gDad.getDeviceID();
        else
            return null;
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
        try{
            final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            int result = googleAPI.isGooglePlayServicesAvailable(activity);
            if(result != ConnectionResult.SUCCESS) {
                if(googleAPI.isUserResolvableError(result)) {
                    googleAPI.getErrorDialog(activity, result,
                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
                }
                return false;
            }
            return true;
        }
        catch(Exception e){
            return false;
        }

    }

    public static boolean isInternetAvailable(){
        return GDutils.internetConnectionAvailable();
    }

}
