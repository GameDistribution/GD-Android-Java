package com.gd.analytics;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

class GDlogrequest {

	protected static ArrayList<GDsendObj> Pool = new ArrayList<GDsendObj>();
	
	protected static void pushLogFirst(GDsendObj _pushAction) {
		Pool.add(0, _pushAction);
	}
	
	protected static void pushLog(GDsendObj _pushAction) {
		// TODO Auto-generated method stub
		int i = 0;
        for (i = 0; i < Pool.size(); i++) {
            if ( Pool.get(i).action == _pushAction.action) {
                if (Pool.get(i).action == "custom" && ((GDcustomLog)Pool.get(i).value).key == ((GDcustomLog)_pushAction.value).key) {
                    ((GDcustomLog)Pool.get(i).value).value++;
                } else {
                    Pool.get(i).value = _pushAction.value;
                }
                break;
            }
        }
        if (i == Pool.size()) Pool.add(_pushAction);
        return;    		
	}

	private enum act {
		cmd, visit , play, custom, ping
	}
	
	private enum res {
		visit , url
	}
	
    protected static void doResponse(GDresponseData _data) {
    	String currentSid = new String(GDlogchannel.postObj.sid).trim();
    	String responseSid = new String(_data.res).trim();
    	
        switch (act.valueOf(_data.act)) {
            case cmd:
            	GDsendObj sendObj = new GDsendObj();
                switch(res.valueOf(_data.res)) {
                    case visit:
                        sendObj.action = "visit";
                        sendObj.value = GDutils.getCookie("visit");
                        sendObj.state = GDutils.getCookie("state");
                        pushLogFirst(sendObj);            
                        break;                    
                    case url:
                        sendObj.action = "cbp";
						try {
							sendObj.value = GDutils.OpenURL(_data.dat.url,_data.dat.target,_data.dat.reopen);
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							sendObj.value = 1501;
						}
                        pushLog(sendObj);						
                        break;
				default:
					break;
                }						
                break;				
            case visit:
                if (responseSid.equals(currentSid)) {
                    int state = GDutils.getCookie("state");
                    state++;
                    GDutils.setCookie("visit",0);
                    GDutils.setCookie("state", state);
                }
                break;
            case play:
                if (responseSid.equals(currentSid)) {
                    GDutils.setCookie("play", 0);
                }
                break;
            case custom:
                if (responseSid.equals(currentSid)) {
                    GDutils.setCookie(_data.custom, 0);
                }
                break;
			default:
				break;
        }
    }
	
}
