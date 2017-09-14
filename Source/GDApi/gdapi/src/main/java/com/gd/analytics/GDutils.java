package com.gd.analytics;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class GDutils {
	private static final String LOG_TAG = "GD";
	private static String OpenedURL = "";
	
	public GDutils() {
		// TODO Auto-generated constructor stub
	}

	protected static void log(String msg) {
		// TODO Auto-generated method stub
		if (GDstatic.debug) Log.i(LOG_TAG, msg);
	}

	protected static String sessionId() {
        String text = new String("");
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        int index = 0;
        for (int i = 0; i < 32; i++) {
        	index = (int)Math.floor(Math.random() * possible.length());
            text = text.concat(possible.substring(index,index+1));
        }
        return text.trim();
	}

	protected static void compareVersions(float andver){
		if(GDstatic.apiVersion < andver){
			log("Your GDApi (version: "+GDstatic.apiVersion+") is out of date. We strongly recommend you to use the newest version ("+andver+") to access all features of GDApi.\nhttps://github.com/GameDistribution/GD-Android-Java");
		}
	}

	protected static int getCookie(String key) {
		try {
			return GDlogger.appSharedPrefs.getInt(key, 0);
		} catch (Exception e) {
			return 0;
		}
	}

	protected static void setCookie(String key, int value) {
		// TODO Auto-generated method stub
		Editor prefsEditor = GDlogger.appSharedPrefs.edit();
		prefsEditor.putInt(key, value);
		prefsEditor.commit();
	}
	
	protected static String[] removeElement(String[] input, String deleteMe) {
		if (input != null) {
			List<String> list = new ArrayList<String>(Arrays.asList(input));
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).equals(deleteMe)) {
					list.remove(i);
				}
			}
			return list.toArray(new String[0]);
		} else {
			return new String[0];
		}
	}	

	protected static String[] removeElementAt(String[] input, int index) {
		if (input != null) {
			List<String> list = new ArrayList<String>(Arrays.asList(input));
			list.remove(index);
			return list.toArray(new String[0]);
		} else {
			return new String[0];
		}
	}

	protected static void fetchData(GDpostObj postObj, GDhttpAyncResponseHandler responseHandler) {
		/*
		RequestParams params = new RequestParams();		
		params.put("act", postObj.act));
				
		httpClient.setTimeout(30000);
		String userAgent = System.getProperty("http.agent", "Android device");
		httpClient.setUserAgent(userAgent);  
		httpClient.setEnableRedirects(true);
		httpClient.addHeader("Referer", "http://www.xxx.com");
		
		GDutils.log("Send action: " + postObj.act);
		
		httpClient.post("http://"+GDstatic.regId+"."+ GDstatic.serverId + ".submityourgame.com/" + GDstatic.sVersion + "/", params, responseHandler);
		*/
		
		GDhttpClient _http = new GDhttpClient();
		GDtaskParams _params = new GDtaskParams();
		
		List<NameValuePair> postParams = new ArrayList<NameValuePair>();
		postParams.add(new BasicNameValuePair("act", postObj.act));
		try {
			postParams.add(new BasicNameValuePair("cbp", postObj.cbp==null?"":URLEncoder.encode(postObj.cbp, "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			postParams.add(new BasicNameValuePair("sid", postObj.sid==null?null:URLEncoder.encode(postObj.sid, "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			postParams.add(new BasicNameValuePair("gid", postObj.gid==null?null:URLEncoder.encode(postObj.gid, "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			postParams.add(new BasicNameValuePair("ref", postObj.ref==null?null:URLEncoder.encode(postObj.ref, "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			postParams.add(new BasicNameValuePair("ver", postObj.ver==null?null:URLEncoder.encode(postObj.ver, "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		_params.url = "http://"+GDstatic.regId+"."+ GDstatic.serverId + ".submityourgame.com/" + GDstatic.sVersion + "/";
		_params.method = GDtaskParams.METHODS.POST;
		_params.params = postParams;
		
		GDutils.log("Send action: " + postObj.act);
		
		_http.execute(_params,responseHandler);
		
	}
		
	protected static int OpenURL(String _url,String _target,Boolean _reopen) throws ExecutionException {
		int res=1500;
        if (GDstatic.enable) {
			if (_reopen) {
				OpenedURL ="";
				res = 1501;
			} else if (OpenedURL!=_url) {
				if (!_url.startsWith("http://") && !_url.startsWith("https://")) {
					_url = "http://" + _url;
				}
				
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(_url));
				GDlogger.mContext.startActivity(browserIntent);
							
				OpenedURL = _url;
				res = 1502;
			}			
			return res;
        } else {
        	return res;
        }
	}		
	
	protected static boolean isApplicationBroughtToBackground() {
		try {
		    ActivityManager am = (ActivityManager) GDlogger.mContext.getSystemService(Context.ACTIVITY_SERVICE);
		    List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		    if (!tasks.isEmpty()) {
		        ComponentName topActivity = tasks.get(0).topActivity;
		        if (!topActivity.getPackageName().equals(GDlogger.mContext.getPackageName())) {
		            return true;
		        }
		    }
		} catch (Exception e) {
		    return false;		
		}
	    return false;
	}

	static <T> boolean typeOf(T value)
	{
		if (value instanceof Integer){
			return true;
		}
		else if (value instanceof String){
			return true;
		}
		else if (value instanceof Boolean){
			return true;
		}
		else
			return false;
	}

	static boolean internetConnectionAvailable() {
		int timeOut = 2000;

		InetAddress inetAddress = null;
		try {
			Future<InetAddress> future = Executors.newSingleThreadExecutor().submit(new Callable<InetAddress>() {
				@Override
				public InetAddress call() {
					try {
						return InetAddress.getByName("google.com");
					} catch (UnknownHostException e) {
						return null;
					}
				}
			});
			inetAddress = future.get(timeOut, TimeUnit.MILLISECONDS);
			future.cancel(true);
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (TimeoutException e) {
		}
		return inetAddress!=null && !inetAddress.equals("");
	}

}
