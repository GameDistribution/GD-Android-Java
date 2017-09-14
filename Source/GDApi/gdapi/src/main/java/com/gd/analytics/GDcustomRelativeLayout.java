package com.gd.analytics;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

class GDcustomRelativeLayout extends RelativeLayout {

    private WebView mWebView;
    private Button mButton;
    private View mView;
    private TextView mText;
    private RelativeLayout mRelativeLayout;
    
    public static final int POPUP_ID = 0X100;
    
	public GDcustomRelativeLayout(Context context) {
        super(context);
                        
        mView = new View(context);
        mView.setId(POPUP_ID);
        mView.setBackgroundColor(0x77000000);
        LayoutParams vparams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        vparams.addRule(RelativeLayout.ALIGN_TOP);
        mView.setLayoutParams(vparams);

        mWebView = new WebView(context);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mWebView.setId(POPUP_ID+1);
        mWebView.setScrollContainer(false);
        this.setPadding(100,100,100,100);

        mButton = new Button(context);
        mButton.setId(POPUP_ID+2);
        mButton.setText("X");
        mButton.setBackgroundColor(Color.WHITE);
        mButton.setMinimumWidth(20);
        mButton.setMinimumHeight(20);
        mButton.setMinWidth(20);
        mButton.setMinHeight(20);

        LayoutParams bparams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        bparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        bparams.addRule(RelativeLayout.ALIGN_TOP, mView.getId());
        mButton.setLayoutParams(bparams);

        mRelativeLayout = new RelativeLayout(context);
        mRelativeLayout.setId(POPUP_ID+3);
        mRelativeLayout.setBackgroundColor(0xFFFFFFFF);
        LayoutParams lparams = new LayoutParams(LayoutParams.MATCH_PARENT,110);
        //lparams.addRule(RelativeLayout.ALIGN_TOP, mView.getId());
        mRelativeLayout.setLayoutParams(lparams);

        mText = new TextView(mRelativeLayout.getContext());
        mText.setId(POPUP_ID+4);
        mText.setText("Advertisement");
        LayoutParams tparams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tparams.addRule(RelativeLayout.CENTER_IN_PARENT, mRelativeLayout.getId());
        mText.setLayoutParams(tparams);

        addView(mView);
        addView(mRelativeLayout);
        addView(mWebView);
        addView(mButton);
        mRelativeLayout.addView(mText);
    }

	public void setResize(int width, int height) {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.width=width*3;
        params.height=height*3+10;
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mWebView.setLayoutParams(params);
	}

    public void setPadding(int left,int top,int right,int bottom) {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.setMargins(left, top+110, right, bottom);
        mWebView.setLayoutParams(params);
    }
    
    public void loadUrl(String url) {
    	mWebView.loadUrl(url);        
    }
    
    public void show() {
    	mView.setVisibility(View.VISIBLE);			
    	mWebView.setVisibility(View.VISIBLE);    
    	mButton.setVisibility(View.VISIBLE);        
    	mRelativeLayout.setVisibility(View.VISIBLE);        	
    }
    
    public void hide() {
    	mView.setVisibility(View.GONE);
    	mWebView.setVisibility(View.GONE);    
    	mButton.setVisibility(View.GONE);     	
    	mRelativeLayout.setVisibility(View.GONE);     	
    }
    
    public View getTheView() {
        return mView;
    }
    
    public WebView getTheWebView() {
        return mWebView;
    }

    public Button getTheButton() {
        return mButton;
    }

    public void setBackgroundColor(int color){
    	if (mView!=null) {
            mView.setBackgroundColor(color);
    	}
    }
}