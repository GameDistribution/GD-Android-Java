package com.gd.analytics;

import com.android.volley.VolleyError;
import com.google.gson.JsonObject;

import org.json.JSONObject;

/**
 * Created by demiremrece on 30.11.2017.
 */

public interface GDHttpCallback {
    void onSuccess(JSONObject data);
    void onError(VolleyError error);
}
