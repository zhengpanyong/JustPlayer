package com.example.zu.myapp.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;

import com.example.zu.myapp.model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zu on 2016/1/25.
 */
public class DBUtil
{
    private static final String DBName="Songs";

    private static final int VERSION=1;
    private static DBUtil dbUtil;
    private SQLiteDatabase db;
    private Context context;

    private DBUtil()
    {

    }

    public synchronized static DBUtil getInstance()
    {
        //此处返回的是这个对象，而不是SQLiteDatabase，这个对象包含了各种方法，而这些方法在db中是没有的
        if(dbUtil==null)
        {
            dbUtil=new DBUtil();

        }
        return dbUtil;
    }

    public void openConnection()
    {
        DBOpenHelper dbHelper=new DBOpenHelper(MyApplication.getContext(),DBName,null,VERSION);
        db=dbHelper.getWritableDatabase();
    }

    public void closeConnection()
    {
        db.close();
    }

    public void cleanTable()
    {
        db.execSQL("drop table if exists ?",new String[]{DBOpenHelper.tableName});
        db.execSQL(DBOpenHelper.sql);
    }

    public void saveSong(Song song)
    {

        String sql="insert into ? values (null,?,?,?,?,?,?,?,?)";


        db.execSQL(sql, new Object[]{song.getSongName(), song.getArtist(), song.getAlbum(),
                song.getBuildTime(), song.getSongSize(), song.getSongDuration(), song.getFileName(), song.getSongPath()});


    }

    public void updateDB(List<Song> songs)
    {
        for(Song song : songs)
        {
            saveSong(song);
        }
    }

    //从本程序的数据库中读取所有歌曲
    public List<Song> listSong()
    {
        Cursor cursor=db.rawQuery("select * from ?",new String[]{DBOpenHelper.tableName});
        List<Song> songs=new ArrayList<>();
        if(cursor.moveToFirst())
        {
            do{

                Song song=new Song();
                song.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                song.setSongName(cursor.getString(cursor.getColumnIndexOrThrow("song_name")));
                song.setArtist(cursor.getString(cursor.getColumnIndexOrThrow("artist")));
                song.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow("album")));
                song.setFileName(cursor.getString(cursor.getColumnIndexOrThrow("file_name")));
                song.setSongDuration(cursor.getInt(cursor.getColumnIndexOrThrow("song_duration")));
                song.setBuildTime(cursor.getString(cursor.getColumnIndexOrThrow("build_time")));
                song.setSongPath(cursor.getString(cursor.getColumnIndexOrThrow("song_path")));
                song.setSongSize(cursor.getInt(cursor.getColumnIndexOrThrow("song_size")));

                songs.add(song);

            }while(cursor.moveToNext());
        }

        cursor.close();
        return songs;
    }

    public void delete(int id)
    {
        String sql="delete from "+DBOpenHelper.tableName+" where id = ?";
        db.execSQL(sql,new String[]{String.valueOf(id)});
    }
}
