package com.gd.analytics;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Created by Emre Demir on 20.09.2016.
 */

public class GDad {

    GDadListener devListener;
    GDshowObj arguments;
    private PublisherInterstitialAd mInterstitialAd;
    private Activity mContext;
    private String mUnitId;
    private String cordovaAdxUnitId = "ca-mb-app-pub-5192618204358860/8119020012";
    private PublisherAdView publisherAdView;
    private FrameLayout rootview;
    private RelativeLayout relativeLayoutContainer;
    private boolean bannerActive = false;
    private boolean isCordovaPlugin = false;

    public void init(Activity mContext) {
        setmContext(mContext);
        setRootview((FrameLayout) mContext.findViewById(android.R.id.content));
        initBannerObject();
    }

    public void init(Activity mContext, boolean isCordovaPlugin) {
        if (isCordovaPlugin) {
            this.isCordovaPlugin = true;
            setmContext(mContext);
            // in cordova plugin, there will be no banner ads.
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

    private void requestBanner(String size, String alignment, String position) {
        Bundle cust_params = new Bundle();
        cust_params.putString("apptype", "android");
        cust_params.putString("appid", GDstatic.gameId);
        cust_params.putString("a", GDstatic.affiliateId);

        PublisherAdRequest adRequest;
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        builder.addNetworkExtrasBundle(AdMobAdapter.class, cust_params);

        adRequest = builder.build();
        handleBannerParams(size, alignment, position);

        publisherAdView.setAdUnitId(getmUnitId());


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

            }
        });
    }

    private void requestInterstitial() {

        Bundle cust_params = new Bundle();
        cust_params.putString("apptype", "android");
        cust_params.putString("appid", GDstatic.gameId);
        cust_params.putString("a", GDstatic.affiliateId);

        PublisherAdRequest adRequest;
        mInterstitialAd = new PublisherInterstitialAd(mContext);

        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        builder.addNetworkExtrasBundle(AdMobAdapter.class, cust_params);

        adRequest = builder.build();

        if (this.isCordovaPlugin) {
            mInterstitialAd.setAdUnitId(cordovaAdxUnitId);
        } else {
            mInterstitialAd.setAdUnitId(getmUnitId());
        }
        mInterstitialAd.setAdListener(new AdListener() {
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
            }
        });
        mInterstitialAd.loadAd(adRequest);
    }

    private void handleBannerParams(String size, String alignment, String position) {

        if (size.equals("AdSize.BANNER")) {
            publisherAdView.setAdSizes(AdSize.BANNER);

        } else if (size.equals("AdSize.LARGE_BANNER")) {
            publisherAdView.setAdSizes(AdSize.LARGE_BANNER);

        } else if (size.equals("AdSize.MEDIUM_RECTANGLE")) {
            publisherAdView.setAdSizes(AdSize.MEDIUM_RECTANGLE);

        } else if (size.equals("AdSize.FULL_BANNER")) {
            publisherAdView.setAdSizes(AdSize.FULL_BANNER);

        } else if (size.equals("AdSize.LEADERBOARD")) {
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

        if (!GDGameData.enableAds) {
            return;
        }

        Gson gson = new Gson();
        final GDshowObj gDshowObj;
        gDshowObj = gson.fromJson(args, GDshowObj.class);
        arguments = gDshowObj;

        GDutils.log("size: " + gDshowObj.size);
        GDutils.log("alignment: " + gDshowObj.alignment);
        GDutils.log("position: " + gDshowObj.position);


        if (gDshowObj.isInterstitial) {
            requestInterstitial();
        } else {
            requestBanner(gDshowObj.size, gDshowObj.alignment, gDshowObj.position);
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
