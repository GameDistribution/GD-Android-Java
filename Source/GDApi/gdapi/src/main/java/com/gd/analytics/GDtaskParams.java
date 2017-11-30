package com.gd.analytics;

import org.apache.http.NameValuePair;

import java.util.List;

class GDtaskParams {
    protected String url = "";
    protected METHODS method;
    protected List<NameValuePair> params = null;

    protected enum METHODS {
        GET, POST
    }

}
