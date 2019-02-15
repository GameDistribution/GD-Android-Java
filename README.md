# GD-Android-Java
Game Distribution Android API

## Include GDApi using Android Studio

There are two required files to use our GDApi:
* gdapi.jar
* google play services

### _Adding gdapi.jar and Google play services as dependency_

Firstly, as you can see at the picture below, after downloading _gdapi.jar_ file from our page, it is supposed to be placed into "libs" folder under your project. Copy _gdapi.jar_ file and paste it into "libs" folder.

![](http://www.gamedistribution.com/images/gd-android/gdand1.png)

Secondly and finally, the library is supposed to be included and compiled at "build.gradle". There are two different ways to do it. The pictures below illustrate how to do it. 

<img src="http://www.gamedistribution.com/images/gd-android/gdand2.png" width="700">
<img src="http://www.gamedistribution.com/images/gd-android/gdand3.png" width="700">

## How to use it

### _Initialize_

There are three parameters required to invoke init function: Game id, user id and activity context which is required to show the ad in.

```
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.gd.analytics.GDlogger;

public class MainActivity extends AppCompatActivity {

    Activity mContext;
    String gameId,userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameId = "xxxxxx";
        userId = "xxxxxx";
        mContext = this;  // context where the user wants to serve the ad in

        GDlogger.debug(true);  // enables log messages related to api at logcat (android monitor)

        GDlogger.init(gameId,userId,mContext); // enables

    }
}

```
### _Request Ad_

There are two types ads which the developer can request for: Interstitial and banner ads.

**Interstitial ads** are full screen ads that cover the interface of their host application. They're typically displayed at natural transition points in the flow of an application, such as between activities or during the pause between levels in a game. 

**Banner ads** are rectangular graphic display that stretches across bottom of a website.

The picture below demonstrates how to request for banner and interstitial ads using GDApi. It uses **_ShowBanner()_** function to request and the function gets parameter as a json string.

```
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.gd.analytics.GDlogger;

public class MainActivity extends AppCompatActivity {
    Activity mContext;
    String gameId,userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameId = "xxxxxx";
        userId = "xxxxxx";
        mContext = this;  // context where the user wants to serve the ad in

        GDlogger.debug(true);  // enables log messages related to api at logcat (android monitor)

        GDlogger.init(gameId,userId,mContext); // enables


        /*
            If pre-roll ad is enable on your account, init shows an interstitial ad as well

         */
        
        Boolean isInterstitial = false;
        GDlogger.ShowBanner(isInterstitial); // request for a standart banner ad, 320x50, at bottom center

        // It is possible to specify size, alignment and position of the banner ad.
        GDlogger.ShowBanner(GDAdSize.BANNER, GDAlignment.CENTER, GDAdPosition.BOTTOM);

       /*
          Alignment: LEFT, CENTER, RIGHT
          Position: TOP, MIDDLE, BOTTOM
       */

        isInterstitial = true;
        GDlogger.ShowBanner(isInterstitial); // request for a interstitial ad
        
        /*
            ShowBanner() can not be invoked for one ad type consecutively. Time limitation can be set on the user's account.
            For instance; you can not invoke ShowBanner for banner again within time limit if it has been already used for a banner request. 
            However, you can request for an interstitial.
            If pre-roll is enabled, init function also requests for an interstitial.
         */

    }
}
```

### _Events_

**onBannerReceived**: This event is fired when an ad is received. Also "adType" of data contains the type of ads loaded.

**onBannerStarted**: This event is fired when an ad starts to show up.

**onBannerClosed**: This event is fired when an ad is closed.

**onBannerFailed**: This event is fired when an ad is failed to load. Also, "message" of data contains why.

**onAPIReady**: This event means the api is ready to serve ads. You can invoke "showBanner".

**onAPINotReady**: When something goes wrong with the api, this event is invoked. Api is supposed to be init again.

**onPreloadFailed**: This event is fired when preloaded ad is failed to show. Also, "message" of data contains why.

**onPreloadedAdCompleted**: This event is fired when preloaded ad is closed.

**onAdPreloaded**: This event is fired when preload ad is received. 


```
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.gd.analytics.GDEvent;
import com.gd.analytics.GDadListener;
import com.gd.analytics.GDlogger;

public class MainActivity extends AppCompatActivity {

    Activity mContext;
    String gameId,userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameId = "xxxxxx";
        userId = "xxxxxx";
        mContext = this;  // context where the user wants to serve the ad in

        GDlogger.debug(true);  // enables log messages related to api at logcat (android monitor)

        GDlogger.setAdListener(new GDadListener() {
            @Override
            public void onAPIReady() {
                super.onAPIReady();
                GDlogger.ShowBanner(false); // request for a standart banner ad 320x50 at bottom center

            }
            //....
        });

        GDlogger.init(gameId,userId,mContext); // enables
    }
}

```













