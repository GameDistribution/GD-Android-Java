package com.gd.analytics;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.util.List;

class GDhttpService {

	private static final int connectionTimeOutSec = 20;
	private static final int socketTimeoutSec = 20;

//	static byte[] response = null;
	static String response = null;


	public Header[] _headers = null;
	public int _statusCode = 0;

	protected String callHttp(String url, GDtaskParams.METHODS method)
			throws Throwable {
		return this.callHttp(url, method, null);
	}

	protected String callHttp(String url, GDtaskParams.METHODS method,	List<NameValuePair> params) throws Throwable {
		// http client
		DefaultHttpClient defaulthttpClient = new DefaultHttpClient();

		final HttpParams httpParameters = defaulthttpClient.getParams();

		HttpConnectionParams.setConnectionTimeout(httpParameters,
				connectionTimeOutSec * 1000);
		HttpConnectionParams.setSoTimeout(httpParameters,
				socketTimeoutSec * 1000);

		HttpEntity httpEntity = null;
		HttpResponse httpResponse = null;

		// Checking http request method type
		if (method == GDtaskParams.METHODS.POST) {
			HttpPost httpPost = new HttpPost(url);

			httpPost.addHeader("User-Agent", System.getProperty("http.agent", "Android device"));
			// adding post params
			if (params != null) {
				httpPost.setEntity(new UrlEncodedFormEntity(params));
			}

			httpResponse = defaulthttpClient.execute(httpPost);

		} else if (method == GDtaskParams.METHODS.GET) {
			// appending params to url
			if (params != null) {
				String paramString = URLEncodedUtils.format(params, "utf-8");
				url += "?" + paramString;
			}
			HttpGet httpGet = new HttpGet(url);

			httpResponse = defaulthttpClient.execute(httpGet);
		}
		
		try {
			httpEntity = httpResponse.getEntity();
			_headers = httpResponse.getAllHeaders();
			_statusCode = httpResponse.getStatusLine().getStatusCode();
			response = EntityUtils.toString(httpEntity);
			//response = EntityUtils.toByteArray(httpEntity);
		} catch (Exception e) {
//			response = new byte[0];
			response = "";
		}
		return response;
	}

	protected Header[] getHeaders() {
		return _headers;
	}

	protected int getStatusCode() {
		return _statusCode;
	}

}
