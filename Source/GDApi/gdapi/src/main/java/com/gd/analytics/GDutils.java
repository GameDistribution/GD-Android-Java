package com.gd.analytics;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
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

}
