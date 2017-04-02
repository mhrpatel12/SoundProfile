package com.appontherocks.soundprofile;

import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by Mihir on 3/24/2017.
 */

public class AppController extends android.app.Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(AppController.this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
