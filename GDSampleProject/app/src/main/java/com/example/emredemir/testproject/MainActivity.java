package com.example.emredemir.testproject;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gd.analytics.GDEvent;
import com.gd.analytics.GDadListener;
import com.gd.analytics.GDlogger;

public class MainActivity extends AppCompatActivity {

    Activity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        Button btnInit = (Button) findViewById(R.id.btnInit);
        btnInit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GDlogger.debug(true);
                GDlogger.init("9c19a2aa1d84e04b0bd4bc888792bd1e", "82B343C2-7535-41F8-A620-C518E96DE8F6-s1", mContext);
                GDlogger.setAdListener(new GDadListener() {
                    @Override
                    public void onBannerClosed() {
                        super.onBannerClosed();
                    }

                    @Override
                    public void onBannerStarted() {
                        super.onBannerStarted();
                    }

                    @Override
                    public void onBannerRecieved(GDEvent data) {
                        super.onBannerRecieved(data);
                    }
                });
            }
        });

        Button btnBanner = (Button) findViewById(R.id.btnRequestBanner);
        btnBanner.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GDlogger.ShowBanner("{isInterstitial:false}");
            }
        });

        Button btnInter = (Button) findViewById(R.id.btnInter);
        btnInter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GDlogger.ShowBanner("{isInterstitial:true}");
            }
        });



    }
}


























