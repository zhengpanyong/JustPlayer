package com.example.zu.myapp.util;

import android.app.Application;
import android.content.Context;

/**
 * Created by zu on 2016/1/30.
 */
public class MyApplication extends Application
{
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
    }

    public static Context getContext()
    {
        return context;
    }
}
