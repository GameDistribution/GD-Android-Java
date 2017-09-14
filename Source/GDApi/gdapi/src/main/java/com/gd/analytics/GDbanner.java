package com.gd.analytics;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.Header;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

class GDbanner {

	private static XmlPullParserFactory xmlFactoryObject;
	private static XmlPullParser myparser;
	private static Thread adBannerTimer,adInterstitialTimer;
	protected static GDbannerData bannerData = new GDbannerData();

	protected static void init() {

		if (GDstatic.enable) {
		  	try {
				xmlFactoryObject = XmlPullParserFactory.newInstance();
				myparser = xmlFactoryObject.newPullParser();
				myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			} catch (XmlPullParserException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			if (!GDutils.isApplicationBroughtToBackground()) {
				loadBanner(new GDhttpAyncResponseHandler() {

							@Override
							public void onFailure(int statusCode, Header[] header,
												  String content, Throwable error) {

								GDutils.log("Banner onFailure: " + statusCode + " "	+ error.getMessage());
							}

							@Override
							public void onSuccess(int statusCode, Header[] header,
									String content) {
								try {
									switch (statusCode) {
									case 200:
										if (content.length()>0) {
											parseXML(content);

											ShowBanner("{_key:'preroll',isInterstitial:true}");


										}
										break;
									case 401:
										break;
									case 403:
										break;
									default:
										break;
									}
//									GDutils.log("Banner onSuccess: " + statusCode + " "	+ content);

								} catch (Exception e) {
									e.printStackTrace();
								}

							}

						});
			}
		}
	}

	protected static void ShowBanner(String args){

		GDutils.compareVersions(bannerData.apiVersion);

		Gson gson = new Gson();
		final GDshowObj gDshowObj;
		gDshowObj = gson.fromJson(args, GDshowObj.class);


		if(bannerData.enable && gDshowObj._key != null && gDshowObj._key.equals("preroll") && GDlogger.gDad !=null){

			if(bannerData.pre){
				GDlogger.gDad.setmUnitId(GDstatic.adUnit);
				GDlogger.gDad.showBanner(args);
			}
			else{
				if(GDlogger.gDad.devListener!= null){
					GDlogger.gDad.devListener.onBannerFailed("Banner request failed: 'Preroll is disabled.'");
				}
			}
		}
		else{ // so this is for midroll request

			if(bannerData.enable && bannerData.showAfterTimeout!=0 && GDlogger.gDad != null){

				if(gDshowObj.isInterstitial){
					if(GDstatic.reqInterstitialEnabled){
						GDlogger.gDad.setmUnitId(GDstatic.adUnit);
						GDlogger.gDad.showBanner(args);
						adInterstitialTimer = null;
						setAdTimer(true); // inter timer
						GDstatic.reqInterstitialEnabled = false;
					}
					else{
						GDutils.log("You can not invoke 'ShowBanner()' within "+ bannerData.showAfterTimeout/60000 +" min(s).");
					}
				}

				else{
					if(GDstatic.reqBannerEnabled){
						GDlogger.gDad.setmUnitId(GDstatic.adUnit);
						GDlogger.gDad.showBanner(args);
						adBannerTimer = null;
						setAdTimer(false); // banner timer
						GDstatic.reqBannerEnabled = false;
					}
					else {
						GDutils.log("You can not invoke 'ShowBanner()' within "+ bannerData.showAfterTimeout/60000 +" min(s).");
					}
				}
			}
			else{
				if(GDlogger.gDad.devListener!= null){
					GDlogger.gDad.devListener.onBannerFailed("Banner request failed: 'Midroll is disabled.'");
				}
			}

		}


	}

	private static void parseXML(String xml) throws XmlPullParserException, IOException {

		myparser.setInput(new StringReader(xml));
		myparser.nextTag();
		myparser.require(XmlPullParser.START_TAG, null, "rs");

		GDutils.log(xml);
	    while (myparser.next() != XmlPullParser.END_DOCUMENT) {
			if (myparser.getEventType() != XmlPullParser.START_TAG) {
			    continue;
			}

			String name=myparser.getName();
			String value=readText(myparser);

			// Banner timeout
			if(name.equals("tim")){
			   bannerData.timeOut = Integer.parseInt((value!=""?value:"10")+"000");
			}
			// Banner Enable?
			else if(name.equals("act")){
				bannerData.enable = ((value!=""?value:"0").equals("1"));
			}
			else if(name.equals("pre")){
				bannerData.pre = ((value!=""?value:"0").equals("1"));
			}
			else if(name.equals("andadt")){
				bannerData.adType = Integer.parseInt(value!=""?value:"0");
			}
			else if(name.equals("sat")){
				bannerData.showAfterTimeout = Integer.parseInt((value!=""?value:"0")+"000")*60;
				if(bannerData.showAfterTimeout == 0){
					GDstatic.reqBannerEnabled = false;
				}
			}
			else if(name.equals("andver")){
				bannerData.apiVersion = Float.parseFloat(value !=""?value:"1.0");
			}
			else if(name.equals("aid")){
				bannerData.affiliateId = value !=""?value:"";
				GDstatic.affiliateId = bannerData.affiliateId;
			}
			else if(name.equals("andadu")){
				bannerData.adUnit = value !=""?value:"";
				GDstatic.adUnit = bannerData.adUnit;
			}
	   }
		GDutils.compareVersions(bannerData.apiVersion);
	}
	
	private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}	
	
	private static void loadBanner(GDhttpAyncResponseHandler responseHandler) {
		GDhttpClient _http = new GDhttpClient();
		GDtaskParams _params = new GDtaskParams();

		_params.url = "http://" + GDstatic.serverId + ".bn.submityourgame.com/"	+ GDstatic.gameId + ".xml" +"?ver=800&url=http://www.gamedistribution.com";
		_params.method = GDtaskParams.METHODS.GET;
		_params.params = null;
		_http.execute(_params, responseHandler);
	}

	private static void setAdTimer (final boolean isInterstitial){

		if (GDstatic.enable) {

			if(isInterstitial){
				adInterstitialTimer = new Thread(new Runnable() {
					public void run() {
						while (true) {
							try {
								Thread.sleep(bannerData.showAfterTimeout);
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
			}
			else {
				adBannerTimer = new Thread(new Runnable() {
					public void run() {
						while (true) {
							try {
								Thread.sleep(bannerData.showAfterTimeout);
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
	private static void adTimerHandler(boolean isInterstitial){
		if(isInterstitial){
			GDstatic.reqInterstitialEnabled = true;
		}
		else{
			GDstatic.reqBannerEnabled = true;
		}
	}
}
