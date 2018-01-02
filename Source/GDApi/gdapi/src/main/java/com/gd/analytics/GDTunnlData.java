package com.gd.analytics;

import android.os.Bundle;

/**
 * Created by demiremrece on 2.01.2018.
 */

public class GDTunnlData {
    private String Adu,Imp,Err;
    private Bundle CustomParams;

    public String getAdu() {
        return Adu;
    }

    public void setAdu(String adu) {
        Adu = adu;
    }

    public String getImp() {
        return Imp;
    }

    public void setImp(String imp) {
        Imp = imp;
    }

    public Bundle getCustomParams() {
        return CustomParams;
    }

    public void setCustomParams(Bundle customParams) {
        CustomParams = customParams;
    }

    public String getErr() {
        return Err;
    }

    public void setErr(String err) {
        Err = err;
    }
}

