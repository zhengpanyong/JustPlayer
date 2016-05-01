package com.example.zu.myapp.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zu on 2016/1/25.
 */
public class DBOpenHelper extends SQLiteOpenHelper
{
    private Context context;
    private static final String dbName="Player";
    public static final String tableName="song";

    public static String sql="create table"+tableName+"(id integer primary key autoincrement,"+
            "song_name text,"+
            "artist text,"+
            "album text,"+
            "build_time text,"+
            "song_size integer,"+
            "song_duration integer,"+
            "file_name text,"+
            "song_path text)";
    public DBOpenHelper(Context context,String DBName,SQLiteDatabase.CursorFactory factory,int version)
    {
        super(context,DBName,factory,version);
        this.context=context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion>oldVersion)
        {
            db.execSQL("drop table if exists ?",new String[]{tableName});
            onCreate(db);
        }
    }


}
