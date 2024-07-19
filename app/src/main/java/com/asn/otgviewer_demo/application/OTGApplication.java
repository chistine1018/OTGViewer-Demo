package com.asn.otgviewer_demo.application;

import android.app.Application;

import java.lang.ref.WeakReference;


public class OTGApplication extends Application {

    private final String TAG = this.getClass().getSimpleName();


    private static WeakReference<OTGApplication> sInstance;

    public static OTGApplication getInstance() {

        if (sInstance != null && sInstance.get() != null) {
            return sInstance.get();
        } else {
            return null;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = new WeakReference<>(this);
    }


}
