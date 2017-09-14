package com.gd.analytics;

import com.google.gson.Gson;

import org.apache.http.Header;

class GDlogchannel {

	protected static GDpostObj postObj = new GDpostObj();
	private static String callbackParam;
	protected static GDsendObj lastAction;

	protected static void init() {
		if (GDstatic.enable) {		
			new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							if (!GDutils.isApplicationBroughtToBackground()) {
								timerHandler();
							}
							Thread.sleep(30000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
	}

	/*
	private static void startTimer() {
		Timer checkerTimer = new Timer(true);
		checkerTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				timerHandler();
			}
		}, 0, 30000);
	}
	*/


	private static void timerHandler() {
		GDutils.log("Timer is working...");
		if (GDstatic.enable) {

			GDsendObj actionArray = GDlogger.ping();
			if (GDlogrequest.Pool.size() > 0) {
				lastAction = actionArray = GDlogrequest.Pool.get(0);
				GDlogrequest.Pool.remove(0);
			}

			postObj.cbp = callbackParam;

			try {
				Gson gson = new Gson();
				postObj.act = gson.toJson(actionArray);
			} catch (Exception e) {
				GDutils.log("gson Error: " + e.getMessage());
			}

			GDutils.fetchData(postObj, new GDhttpAyncResponseHandler() {			
				
				@Override
				public void onFailure(int statusCode, Header[] header, String content, Throwable error) {
					GDutils.log("fetchData onFailure: " + statusCode + " " + error.getMessage());
					
					if (lastAction != null && lastAction.action!="visit") {					
						GDlogrequest.pushLog(lastAction);						
					}
					
	            	GDsendObj sendObj = new GDsendObj();
                    sendObj.action = "visit";
                    sendObj.value = GDutils.getCookie("visit");
                    sendObj.state = GDutils.getCookie("state");					
					GDlogrequest.pushLogFirst(sendObj);
				}

				@Override
				public void onSuccess(int statusCode, Header[] header, String content) {
					try {
						switch (statusCode) {
						case 200:
							onCompleted(new String(content));
							break;
						case 401:

							break;
						case 403:

							break;
						default:
							break;
						}
						
						lastAction = null;
						
						GDutils.log("fetchData onSuccess: " + statusCode + " " + new String(content));

					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}

			});

		}
	}

	private static void onCompleted(String data) {
		if (data != null && data != "") {
			try {
				Gson gson = new Gson();
				GDresponseData vars = gson.fromJson(data, GDresponseData.class);
				GDlogrequest.doResponse(vars);
				callbackParam = vars.cbp;
			} catch (Exception e) {
				GDutils.log("onCompleted JSON Error: " + e.getMessage());
				GDlogger.visit();
			}
		}
	}
}
