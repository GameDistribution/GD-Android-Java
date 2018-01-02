package com.gd.analytics;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


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
    ArrayList<GDTunnlData> tunnlData;
    int currentRequestInd = -1;
    GDRequestAdHandler gdRequestAdHandler;

    public void init(Activity mContext) {
        setmContext(mContext);
        setRootview((FrameLayout) mContext.findViewById(android.R.id.content));
        initBannerObject();

        if(GDGameData.preRoll){
            showBanner("{isInterstitial:" + true + "}");
        }

        disableSSLCertificateChecking();

    }

    public void init(Activity mContext, boolean isCordovaPlugin) {
        if (isCordovaPlugin) {
            setmContext(mContext);
            // in cordova plugin, there will be no banner ads.

            if(GDGameData.preRoll){
                showBanner("{isInterstitial:" + true + "}");
            }

            disableSSLCertificateChecking();

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
        publisherAdView.setAdUnitId(getmUnitId());
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

                    if (devListener != null)
                        devListener.onBannerFailed(error);

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

                if (devListener != null)
                    devListener.onBannerFailed(error);

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
                showInterstitialAd();

                GDEvent gdEvent = new GDEvent();
                gdEvent.isInterstitial = true;
                gdEvent.dimensions = "Out-of-page";

                if (devListener != null)
                    devListener.onBannerRecieved(gdEvent);

                gdRequestAdHandler.Succes();
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
            if(gDshowObj.isInterstitial)
                this.setmUnitId(GDstatic.testInterUnitId);
            else
                this.setmUnitId(GDstatic.testBannerUnitId);
        }
        else{

            String bundleId = "bundle.test.1";
            String dimension = "640x480";
            if(!gDshowObj.isInterstitial) dimension = gDshowObj.size;
            String url = "http://pub.tunnl.com/oppm?bundleid="+bundleId+"&dnumber="+dimension;

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

                        requestHandler(gDshowObj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        GDutils.log("Something went wrong parsing json game data.\nData:\n"+data.toString());
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
                GDHttpRequest.sendStringRequest(GDlogger.mContext, tunnlData.get(currentRequestInd).getImp(), Request.Method.GET, null, new GDHttpCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                    }

                    @Override
                    public void onError(VolleyError error) {
                    }
                });
            }

            @Override
            public void Error(String err) {

                //ToDO add use error listener here for user.

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
                }

            }
        };


    }

    private static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        } };

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
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

}
