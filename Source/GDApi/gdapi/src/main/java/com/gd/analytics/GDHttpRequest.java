package com.gd.analytics;

import android.content.Context;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonObject;

import org.json.JSONObject;

/**
 * Created by demiremrece on 30.11.2017.
 */

public class GDHttpRequest {

    public static void sendHttpRequest (Context context, String url, int method, JsonObject data, final GDHttpCallback callback){

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (method, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error);
                    }
                });

        GDHttp.getInstance(context).addToRequestQueue(jsObjRequest);

    }

}
