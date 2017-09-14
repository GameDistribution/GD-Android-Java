package com.gd.analytics;

import android.os.AsyncTask;

class GDhttpClient extends AsyncTask<GDtaskParams, Void,String> {

	GDhttpAyncResponseHandler _responseHandler;
	GDhttpService hs;
	
	@Override
	protected  String doInBackground(GDtaskParams... params) {
		hs = new GDhttpService();
		try {
			return hs.callHttp(params[0].url,params[0].method,params[0].params);
		} catch (Throwable e) {
			_responseHandler.onFailure(0, hs.getHeaders() , e.getMessage(),e);
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(String result) {
		if (result!=null) {
			_responseHandler.onSuccess(hs.getStatusCode(), hs.getHeaders(), result);	
		}
	}
	
	public void execute(GDtaskParams _params, GDhttpAyncResponseHandler responseHandler) {
		// TODO Auto-generated method stub
		_responseHandler = responseHandler;
		execute(_params);
		
	}
}
