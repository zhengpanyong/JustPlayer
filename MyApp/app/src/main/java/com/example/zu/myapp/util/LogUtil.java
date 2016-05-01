package com.example.zu.myapp.util;

import android.util.Log;
/**
 * Created by zu on 2016/1/24.
 */
public class LogUtil
{
    private static int LEVEL_V=0;
    private static int LEVEL_I=1;
    private static int LEVEL_D=2;
    private static int LEVEL_W=3;
    private static int LEVEL_E=4;

    private static int currentLevel=-1;

    public static void v(String tag,String msg)
    {
        if(currentLevel>=LEVEL_V)
        {
            Log.v(tag,msg);
        }
    }

    public static void i(String tag,String msg)
    {
        if(currentLevel>=LEVEL_I)
        {
            Log.i(tag, msg);
        }
    }

    public static void d(String tag,String msg)
    {
        if(currentLevel>=LEVEL_D)
        {
            Log.d(tag, msg);
        }
    }

    public static void w(String tag,String msg)
    {
        if(currentLevel>=LEVEL_W)
        {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag,String msg)
    {
        if(currentLevel>=LEVEL_E)
        {
            Log.e(tag, msg);
        }
    }

    public static void setCurrentLevel(final int level)
    {
        currentLevel=level;
    }
}
