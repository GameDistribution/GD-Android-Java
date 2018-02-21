package com.gd.analytics;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


/**
 * Created by Emre Demir on 20.09.2016.
 */

public class GDad {

    GDadListener devListener;
    private PublisherInterstitialAd mInterstitialAd;
    private Activity mContext;
    private String mUnitId;
    private PublisherAdView publisherAdView;
    private FrameLayout rootview;
    private RelativeLayout relativeLayoutContainer;
    private boolean bannerActive = false;
    private boolean isPreloadStream = false;
    ArrayList<GDTunnlData> tunnlData;
    private int currentRequestInd = -1;
    private GDRequestAdHandler gdRequestAdHandler;

    public void init(Activity mContext) {
        setmContext(mContext);
        setRootview((FrameLayout) mContext.findViewById(android.R.id.content));
        initBannerObject();

        if (devListener != null) devListener.onAPIReady();

        requestPreloadAd();

    }

    public void init(Activity mContext, boolean isCordovaPlugin) {
        if (isCordovaPlugin) {
            setmContext(mContext);
            // in cordova plugin, there will be no banner ads.

            if (devListener != null) devListener.onAPIReady();

            requestPreloadAd();

        } else {
            init(mContext);
        }
    }

    private void initBannerObject() {
        /* Ad request object for banners*/
        RelativeLayout rl = new RelativeLayout(getmContext());
        rl.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        ));

        setRelativeLayoutContainer(rl);

        publisherAdView = new PublisherAdView(getmContext());
        publisherAdView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        ));

        setPublisherAdView(publisherAdView);

        // add adcontainer into rootview
        getRelativeLayoutContainer().addView(publisherAdView);

        getRootview().addView(relativeLayoutContainer);

    }

    private void requestBanner(String size, String alignment, String position, Bundle customParams, String unitId) {

        if(getRelativeLayoutContainer() != null){

            if(publisherAdView != null){

                getRelativeLayoutContainer().removeView(publisherAdView);
                publisherAdView = new PublisherAdView(getmContext());
                publisherAdView.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                ));
                setPublisherAdView(publisherAdView);
                // add adcontainer into rootview
                getRelativeLayoutContainer().addView(publisherAdView);
            }

            PublisherAdRequest adRequest;
            PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
            builder.addNetworkExtrasBundle(AdMobAdapter.class, customParams);

            adRequest = builder.build();
            handleBannerParams(size, alignment, position);

            publisherAdView.setAdUnitId(unitId);

            publisherAdView.loadAd(adRequest);
            publisherAdView.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    GDutils.log("Ad closed.");
                    if (devListener != null)
                        devListener.onBannerClosed();
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    super.onAdFailedToLoad(errorCode);
                    String error = "";
                    switch (errorCode) {
                        case 0:
                            error += "Internal Error.\nSomething happened internally; for instance, an invalid response was received from the ad server.\nConstant Value: " + errorCode;
                            break;
                        case 1:
                            error += "Invalid request.\nThe ad request was invalid; for instance, the ad unit ID was incorrect.\nConstant Value: " + errorCode;
                            break;
                        case 2:
                            error += "Network error.\nThe ad request was unsuccessful due to network connectivity.\nConstant Value: " + errorCode;
                            break;
                        case 3:
                            error += "No fill.\nThe ad request was successful, but no ad was returned due to lack of ad inventory.\nConstant Value: " + errorCode;
                            break;
                    }

                    GDutils.log("Ad failed to load: " + error);
                    GDutils.log("For more details: https://developers.google.com/android/reference/com/google/android/gms/ads/AdRequest");

                    if (devListener != null && GDstatic.testAds){
                        devListener.onBannerFailed(error);
                    }
                    else if(devListener != null && gdRequestAdHandler != null)
                        gdRequestAdHandler.Error(error);

                }

                @Override
                public void onAdLeftApplication() {
                    super.onAdLeftApplication();
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    if (devListener != null)
                        devListener.onBannerStarted();
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    GDutils.log("Ad received.");
                    bannerActive = true;

                    GDEvent gdEvent = new GDEvent();
                    gdEvent.isInterstitial = false;
                    gdEvent.dimensions = publisherAdView.getAdSize();
                    if (devListener != null)
                        devListener.onBannerRecieved(gdEvent);

                    if(!GDstatic.testAds)
                        gdRequestAdHandler.Succes();

                }
            });
        }
    }

    private void requestInterstitial(Bundle customParams, String unitId) {
        PublisherAdRequest adRequest;
        mInterstitialAd = new PublisherInterstitialAd(mContext);

        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        builder.addNetworkExtrasBundle(AdMobAdapter.class, customParams);
        adRequest = builder.build();

        mInterstitialAd.setAdUnitId(unitId);
        mInterstitialAd.setAdListener(  new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                GDutils.log("Ad closed.");
                if (devListener != null)
                    devListener.onBannerClosed();

                if(isPreloadStream){
                    requestPreloadAd();
                }
                if(isPreloadStream && GDlogger.gDad.devListener != null){
                    GDlogger.gDad.devListener.onPreloadedAdCompleted();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                String error = "";
                switch (errorCode) {
                    case 0:
                        error += "Internal Error.\nSomething happened internally; for instance, an invalid response was received from the ad server.\nConstant Value: " + errorCode;
                        break;
                    case 1:
                        error += "Invalid request.\nThe ad request was invalid; for instance, the ad unit ID was incorrect.\nConstant Value: " + errorCode;
                        break;
                    case 2:
                        error += "Network error.\nThe ad request was unsuccessful due to network connectivity.\nConstant Value: " + errorCode;
                        break;
                    case 3:
                        error += "No fill.\nThe ad request was successful, but no ad was returned due to lack of ad inventory.\nConstant Value: " + errorCode;
                        break;
                }
                GDutils.log("Ad failed to load: " + error);
                GDutils.log("For more details: https://developers.google.com/android/reference/com/google/android/gms/ads/AdRequest");

                if (devListener != null && GDstatic.testAds){
                    devListener.onBannerFailed(error);
                }
                else if(devListener != null && gdRequestAdHandler != null)
                    gdRequestAdHandler.Error(error);

            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                if (devListener != null)
                    devListener.onBannerStarted();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                GDutils.log("Ad received.");

                if(!isPreloadStream())
                    showInterstitialAd();

                GDEvent gdEvent = new GDEvent();
                gdEvent.isInterstitial = true;
                gdEvent.dimensions = "Out-of-page";

                if (devListener != null)
                    devListener.onBannerRecieved(gdEvent);

                if(!GDstatic.testAds)
                    gdRequestAdHandler.Succes();

                if(isPreloadStream && GDlogger.gDad.devListener != null){
                    GDlogger.gDad.devListener.onAdPreloaded();
                }
            }
        });
        mInterstitialAd.loadAd(adRequest);
    }

    private void handleBannerParams(String size, String alignment, String position) {

        if (size.equals("320x50")) {
            publisherAdView.setAdSizes(AdSize.BANNER);

        } else if (size.equals("320x100")) {
            publisherAdView.setAdSizes(AdSize.LARGE_BANNER);

        } else if (size.equals("300x250")) {
            publisherAdView.setAdSizes(AdSize.MEDIUM_RECTANGLE);

        } else if (size.equals("468x60")) {
            publisherAdView.setAdSizes(AdSize.FULL_BANNER);

        } else if (size.equals("728x90")) {
            publisherAdView.setAdSizes(AdSize.LEADERBOARD);

        }

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getRelativeLayoutContainer().getLayoutParams();

        if (position.equals("top")) {
            params.gravity = Gravity.TOP;

        } else if (position.equals("middle")) {
            params.gravity = Gravity.CENTER_VERTICAL;

        } else if (position.equals("bottom")) {
            params.gravity = Gravity.BOTTOM;

        }
        if (alignment.equals("center")) {
            params.gravity = params.gravity | Gravity.CENTER;

        } else if (alignment.equals("left")) {
            params.gravity = params.gravity | Gravity.LEFT;

        } else if (alignment.equals("right")) {
            params.gravity = params.gravity | Gravity.RIGHT;

        }
        getRelativeLayoutContainer().setLayoutParams(params);
    }

    public void showInterstitialAd() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    public void showBanner(String args) {

        if (!GDGameData.enableAds && !GDstatic.testAds) {
            return;
        }

        Gson gson = new Gson();
        final GDshowObj gDshowObj;
        gDshowObj = gson.fromJson(args, GDshowObj.class);

        if(GDstatic.testAds){
            if(gDshowObj.isInterstitial){
                this.setmUnitId(GDstatic.testInterUnitId);
                requestInterstitial(null, GDstatic.testInterUnitId);
            }
            else{
                this.setmUnitId(GDstatic.testBannerUnitId);
                requestBanner(gDshowObj.size, gDshowObj.alignment, gDshowObj.position ,null, GDstatic.testBannerUnitId);
            }
        }
        else{

            currentRequestInd = -1;
            String bundleId = GDGameData.bundleId;
            String msize = "interstitial";
            if(!gDshowObj.isInterstitial) msize = gDshowObj.size;
            String url = "http://pub.tunnl.com/oppm?bundleid=and."+bundleId+"&msize="+msize;

            // getting unit id from tunnl for ad request
            GDHttpRequest.sendHttpRequest(GDlogger.mContext, url, Request.Method.GET, null, new GDHttpCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        JSONArray items = data.getJSONArray("Items");
                        tunnlData = new ArrayList<>();
                        GDTunnlData gdTunnlData;

                        for(int i=0; i<items.length();i++){
                            JSONObject object = (JSONObject) items.get(i);
                            gdTunnlData = new GDTunnlData();
                            gdTunnlData.setAdu(object.getString("Adu"));
                            gdTunnlData.setErr(object.getString("Err"));
                            gdTunnlData.setImp(object.getString("Imp"));

                            JSONArray custom_params = object.getJSONArray("CustomParams");
                            Bundle customParams = new Bundle();
                            for(int j=0; j< custom_params.length();j++){
                                JSONObject obj = (JSONObject) custom_params.get(j);
                                customParams.putString(obj.getString("Key"),obj.getString("Value"));
                            }
                            gdTunnlData.setCustomParams(customParams);
                            tunnlData.add(gdTunnlData);
                        }

                        if(items.length() > 0)
                            requestHandler(gDshowObj);
                        else{
                            if (devListener != null)
                                devListener.onBannerFailed("Something went wrong fetching advertisement settings. Please contact with support team.");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        GDutils.log("Something went wrong parsing json tunnl data.\nData:\n"+data.toString());
                    }
                }
                @Override
                public void onError(VolleyError error) {
                    GDutils.log("Something went wrong fetching unit id from tunnl.");

                }
            });
        }
    }

    private void requestHandler(final GDshowObj gDshowObj){

        currentRequestInd ++;
        GDTunnlData data = tunnlData.get(currentRequestInd);
        if (gDshowObj.isInterstitial) {
            requestInterstitial(data.getCustomParams(),data.getAdu());
        } else {
            requestBanner(gDshowObj.size, gDshowObj.alignment, gDshowObj.position, data.getCustomParams(), data.getAdu());
        }

        gdRequestAdHandler = new GDRequestAdHandler() {
            @Override
            public void Succes() {
                GDHttpRequest.sendStringRequest(GDlogger.mContext, tunnlData.get(currentRequestInd).getImp().replace("https", "http"), Request.Method.GET, null, new GDHttpCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        GDutils.log("Imp");
                    }

                    @Override
                    public void onError(VolleyError error) {
                        GDutils.log("Imp error");
                    }
                });
            }

            @Override
            public void Error(String err) {

                String url = tunnlData.get(currentRequestInd).getErr().replace("https","http");
                GDHttpRequest.sendStringRequest(GDlogger.mContext, url, Request.Method.GET, null, new GDHttpCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                    }

                    @Override
                    public void onError(VolleyError error) {
                    }
                });

                currentRequestInd ++;
                if(tunnlData != null && tunnlData.size()>0 && currentRequestInd < tunnlData.size()){
                    GDTunnlData data = tunnlData.get(currentRequestInd);

                    if (gDshowObj.isInterstitial) {
                        requestInterstitial(data.getCustomParams(),data.getAdu());
                    } else {
                        requestBanner(gDshowObj.size, gDshowObj.alignment, gDshowObj.position, data.getCustomParams(), data.getAdu());
                    }
                }
                else{
                    currentRequestInd = -1;
                    tunnlData = null;

                    if (devListener != null)
                        devListener.onBannerFailed(err);

                }

            }
        };


    }

    public void requestPreloadAd(){
        if(isPreloadStream()){
            String args = "{isInterstitial: true}";
            showBanner(args);
        }
    }

    public void destroyBanner() {
        if (publisherAdView != null && bannerActive) {
            publisherAdView.destroy();
            bannerActive = false;
        }
    }

    public void setAdListener(GDadListener adListener) {
        this.devListener = adListener;
    }

    public PublisherInterstitialAd getmInterstitialAd() {
        return mInterstitialAd;
    }

    public void setmInterstitialAd(PublisherInterstitialAd mInterstitialAd) {
        this.mInterstitialAd = mInterstitialAd;
    }

    public Activity getmContext() {
        return mContext;
    }

    public void setmContext(Activity mContext) {
        this.mContext = mContext;
    }

    public String getmUnitId() {
        return mUnitId;
    }

    public void setmUnitId(String mUnitId) {
        this.mUnitId = mUnitId;
    }

    public PublisherAdView getPublisherAdView() {
        return publisherAdView;
    }

    public void setPublisherAdView(PublisherAdView publisherAdView) {
        this.publisherAdView = publisherAdView;
    }

    public FrameLayout getRootview() {
        return rootview;
    }

    public void setRootview(FrameLayout rootview) {
        this.rootview = rootview;
    }

    public RelativeLayout getRelativeLayoutContainer() {
        return relativeLayoutContainer;
    }

    public void setRelativeLayoutContainer(RelativeLayout relativeLayoutContainer) {
        this.relativeLayoutContainer = relativeLayoutContainer;
    }

    public boolean isPreloadStream() {
        return isPreloadStream;
    }

    public void setPreloadStream(boolean preloadStream) {
        isPreloadStream = preloadStream;
    }

    public boolean isPreloadedAdExist(){
        if (getmInterstitialAd() == null) return false;
        return getmInterstitialAd().isLoaded() && isPreloadStream();
    }

    public boolean isPreloadedAdLoading(){
        if (getmInterstitialAd() == null) return false;
        return  getmInterstitialAd().isLoading() && isPreloadStream();
    }
}
