package com.gd.analytics;

import org.apache.http.Header;

abstract class GDhttpAyncResponseHandler {
	
	protected abstract void onFailure(int statusCode, Header[] header, String content, Throwable error);

	protected abstract void onSuccess(int statusCode, Header[] header, String content);
		
}
