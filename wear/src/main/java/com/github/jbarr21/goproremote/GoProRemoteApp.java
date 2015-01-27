package com.github.jbarr21.goproremote;

import android.app.Application;
import android.util.Log;

import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class GoProRemoteApp extends Application {

    private static GoProRemoteApp instance;

    public static GoProRemoteApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        }
        Timber.d("JIM - creating the app");
    }

    @Override
    public void onTerminate() {
        Log.d("GoProRemoteApp", "JIM - terminating the app");
        super.onTerminate();
    }
}
