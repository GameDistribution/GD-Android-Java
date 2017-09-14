package com.gd.analytics;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import org.apache.http.Header;

/**
 * Created by Emre Demir on 26.09.2016.
 */

public class GDgdp {

    //    private var LocalStorage:SharedObject=SharedObject.getLocal("GameDistribution");
    private SharedPreferences localStorage;
    private int adsPriceLevelRequested = 0;
    private int oldAdsPriceLevelRequested = 0;
    private int adsRetryCount = 0;
    private int oldAdsRetryCount = 0;
    private String gdpString = "{g:0,i:0,l:3,n:3,m:4,su:1,ed:1,sui:1,edd:1,lp:false}";
    private GDgdpModel gdp;

    public GDgdp(){

        localStorage = PreferenceManager.getDefaultSharedPreferences(GDlogger.mContext.getApplicationContext());
        gdp = (GDgdpModel) getCookie("gdp",GDgdpModel.class,null);

        adsPriceLevelRequested=gdp.l;
        oldAdsPriceLevelRequested=gdp.l;

        success(3);

 //       Log.i("GDControl",localStorage.getString("gdp", "{}"));

    }


    public void success(int level){

        oldAdsPriceLevelRequested=adsPriceLevelRequested;
        setCookie("gdp.ed",gdp.ed);

        int su = (Integer) getCookie("gdp.su",Integer.class,gdp.su);
        su--;
        if(su<=0) {
            setCookie("gdp.su",gdp.su);
        }
        else {
            setCookie("gdp.su",su);
        }

        if (gdp.lp) {
            adsPriceLevelRequested+=1;
            if(adsPriceLevelRequested>gdp.m) adsPriceLevelRequested=gdp.n;

            setCookie("gdp.l",adsPriceLevelRequested);
        }
        else if(su<=0){
            adsPriceLevelRequested+=gdp.sui;
            if(adsPriceLevelRequested>gdp.m) adsPriceLevelRequested=gdp.m;

            setCookie("gdp.l",adsPriceLevelRequested);
        }

        oldAdsRetryCount=adsRetryCount;

        Uri.Builder uriBuilder =  new Uri.Builder();
        uriBuilder.appendQueryParameter("a","5"); // (client id )application must be 5!
        uriBuilder.appendQueryParameter("v","2");
        uriBuilder.appendQueryParameter("e","1");
        uriBuilder.appendQueryParameter("g",""+gdp.g);
        uriBuilder.appendQueryParameter("l",""+level);
        uriBuilder.appendQueryParameter("r",""+adsRetryCount);
        uriBuilder.appendQueryParameter("aid","A-GAMEDIST");


        sendToServer(uriBuilder,new GDhttpAyncResponseHandler() {
            @Override
            protected void onFailure(int statusCode, Header[] header, String content, Throwable error) {
                // do nothing
            }

            @Override
            protected void onSuccess(int statusCode, Header[] header, String content) {
                // do nothing
            }
        });

        adsRetryCount=0;
    }

    public void error(int adsErrorCode,int level){

        int oldAdsPriceLevelRequested = adsPriceLevelRequested;

        setCookie("gdp.su",gdp.su);

        int ed = (Integer) getCookie("gdp.ed",Integer.class,gdp.ed);
        ed--;
        if(ed<=0) {
            setCookie("gdp.ed",gdp.ed);
        }
        else {
            setCookie("gdp.ed",ed);
        }

        if (gdp.lp) {
            adsPriceLevelRequested+=1;
            if(adsPriceLevelRequested>gdp.m) adsPriceLevelRequested=gdp.n;

            setCookie("gdp.l",adsPriceLevelRequested);
        }
        else if(ed<=0){
            adsPriceLevelRequested-=gdp.edd;
            if(adsPriceLevelRequested<gdp.n)
                adsPriceLevelRequested=gdp.n;

            setCookie("gdp.l",adsPriceLevelRequested);
        }

        adsRetryCount++;

        Uri.Builder uriBuilder =  new Uri.Builder();
        uriBuilder.appendQueryParameter("a","5"); // application must be 5!
        uriBuilder.appendQueryParameter("v","2");
        uriBuilder.appendQueryParameter("e","0");
        uriBuilder.appendQueryParameter("g",""+gdp.g);
        uriBuilder.appendQueryParameter("l",""+level);
        uriBuilder.appendQueryParameter("r",""+adsErrorCode);
        uriBuilder.appendQueryParameter("aid","A-GAMEDIST");

        sendToServer(uriBuilder,new GDhttpAyncResponseHandler() {
            @Override
            protected void onFailure(int statusCode, Header[] header, String content, Throwable error) {
                // do nothing
            }

            @Override
            protected void onSuccess(int statusCode, Header[] header, String content) {
                // do nothing
            }
        });

    }


    public void request(final GDCallback gdCallback){
        if (!GDutils.isApplicationBroughtToBackground()) {

            Uri.Builder uriBuilder =  new Uri.Builder();
            uriBuilder.appendQueryParameter("a","5"); // application must be 5!
            uriBuilder.appendQueryParameter("aid","A-GAMEDIST");

            sendToServer(uriBuilder,new GDhttpAyncResponseHandler() {

                @Override
                public void onFailure(int statusCode, Header[] header, String content, Throwable error) {
                    gdCallback.callback(adsPriceLevelRequested);
                }

                @Override
                public void onSuccess(int statusCode, Header[] header, String content) {
                    try {
                        switch (statusCode) {
                            case 200:
                                if (content.length()>0) {
                                    Gson gson = new Gson();
                                    GDgdpModel data = gson.fromJson(content, GDgdpModel.class);

                                    if(data.i!=gdp.i){
                                        gdp=data;
                                        setCookie("gdp",gdp);

                                        adsPriceLevelRequested=gdp.l;
                                        setCookie("gdp.l",adsPriceLevelRequested);
                                    }

                                    gdCallback.callback(adsPriceLevelRequested);
                                }
                                break;
                            case 401:
                                break;
                            case 403:
                                break;
                            default:
                                break;
                        }

                        GDutils.log("Banner onSuccess: " + statusCode + " "	+ content);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            });
        }
    }
    private void sendToServer(Uri.Builder uriBuilder,GDhttpAyncResponseHandler responseHandler) {

        String url="https://adtag.tunnl.com/collect";

        GDhttpClient _http = new GDhttpClient();
        GDtaskParams _params = new GDtaskParams();

        _params.url = url + (uriBuilder !=null ? uriBuilder.toString():"");
        _params.method = GDtaskParams.METHODS.GET;
        _params.params = null;
        _http.execute(_params, responseHandler);

    }

    private <T> Object getCookie(String key, Class<T> name, Object defaultVal) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(localStorage.getString(key, "{}"), name);
        } catch (Exception e) {

            return defaultVal != null ? defaultVal : null;
        }
    }
    private void setCookie(String key,Object value) {
        Gson gson = new Gson();
        SharedPreferences.Editor prefsEditor = localStorage.edit();
        prefsEditor.putString(key, gson.toJson(value));
        prefsEditor.commit();
    }
}
