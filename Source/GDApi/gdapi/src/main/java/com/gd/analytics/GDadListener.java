package com.gd.analytics;

/**
 * Created by Emre Demir on 20.09.2016.
 */

public abstract class GDadListener {

    public void onBannerClosed() {
    }
    public void onBannerStarted() {
    }
    public void onBannerRecieved(GDEvent data) {
    }
    public void onBannerFailed(String msg) {
    }
    public void onAPIReady(){
    }
    public void onAPINotReady(String error){
    }

}
