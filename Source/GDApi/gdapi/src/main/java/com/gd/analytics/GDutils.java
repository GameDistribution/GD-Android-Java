package com.gd.analytics;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.List;

final class GDutils {
    private static final String LOG_TAG = "GD";

    public GDutils() {
        // TODO Auto-generated constructor stub
    }

    protected static void log(String msg) {
        // TODO Auto-generated method stub
        if (GDstatic.debug) Log.i(LOG_TAG, msg);
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

    public static boolean isOnline(Activity mContext) {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
