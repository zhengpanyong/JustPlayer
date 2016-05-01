package com.example.zu.myapp.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.provider.MediaStore;

import com.example.zu.myapp.model.Song;
import com.example.zu.myapp.service.StatusService;

import org.apache.http.HttpConnection;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zu on 2016/1/26.
 */
public class FileUtil
{


    private  ArrayList<OnLrcCompleteListener> listeners=new ArrayList<>();

    /*
    *Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    *Cursor cursor = query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    *Int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
    *String tilte = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
    *歌曲ID：MediaStore.Audio.Media._ID
    * 歌曲的名称 ：MediaStore.Audio.Media.TITLE
    * 歌曲的专辑名：MediaStore.Audio.Media.ALBUM
    * 歌曲的歌手名： MediaStore.Audio.Media.ARTIST
    *歌曲文件的全路径 ：MediaStore.Audio.Media.DATA
    *歌曲文件的名称：MediaStore.Audio.Media.DISPLAY_NAME
    * 歌曲文件的发行日期：MediaStore.Audio.Media.YEAR
    * 歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
    * 歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
    * */

//   从MediaStore里读取媒体文件信息，返回List
    public static List<Song> loadFromMediaStore(Context context)
    {



//        MediaScannerConnection.scanFile(context, new String[]{Environment
//                .getExternalStorageDirectory().getAbsolutePath()}, null, null);


        List<Song> songs=new ArrayList<>();
        ContentResolver contentResolver=context.getContentResolver();
        Cursor cursor=contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
               null,null,null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        if(cursor.moveToFirst())
        {
            do{
                if(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))!=null && Integer.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)))>60000)
                {
                    Song song=new Song();
                    song.setSongName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
                    song.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                    song.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
                    song.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
                    song.setSongDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                    song.setBuildTime(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)));
                    song.setSongPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                    song.setSongSize(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)));
                    song.setAlbumId(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));
                    song.setId(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
                    songs.add(song);
                }

            }while(cursor.moveToNext());
        }
        cursor.close();
        return songs;

    }
//    更新MediaStore
    public static void updateProvider()
    {

    }

//    删除歌曲
    public static void deleteSong(Song song)
    {

    }

//    更新歌曲信息
    public static void updateSong(Song song)
    {

    }

    public interface OnLrcCompleteListener
    {
        void onComplete(int resultCode,File lrcFile);
    }

    public void setOnLrcCompleteListener(OnLrcCompleteListener listener)
    {
        listeners.add(listener);
    }

    public void removeOnHttpLrcCompleteListener(OnLrcCompleteListener listener)
    {
        listeners.remove(listener);
    }

    private void myNotifyAll(int resultCode,File file)
    {
        for(OnLrcCompleteListener listener:listeners)
        {
            listener.onComplete(resultCode,file);
        }
    }



    /*
    * 所用歌词api为歌词迷，http://api.geci.me/en/latest/
    * */
    public void getLrc(final String songName,final String artist)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sdCard=Environment.getExternalStorageDirectory().getAbsolutePath();
                if(sdCard!=null)
                {
                    String lrcName=sdCard+ StatusService.APP_ROOT_DIR+StatusService.LRC_PATH+"/"+songName+"-"+artist+".lrc";
                    File lrc=new File(lrcName);
                    HttpURLConnection conn=null;
                    URL url=null;
                    if (!lrc.exists())
                    {
                        //TODO:从网站获取歌词，然后保存到本地，下一次直接从本地读取.另外，网络可能造成比较大的延迟，所以应该改用多线程

                        /*
                        * 如果歌词文件不存在，就从网上找到并且下载下来，先存为文件，然后再将该文件传到调用者
                        * */
                        try
                        {
                            /*
                            * 第一次得到的是一个index表，从表中选择歌词版本，这里总是选择第一个
                            * */
                            url=new URL("http://geci.me/api/lyric/"+songName+"/"+artist);
                            conn=(HttpURLConnection)url.openConnection();
                            conn.setConnectTimeout(3000);
                            conn.setRequestMethod("GET");
                            if(conn.getResponseCode()==200)
                            {
                                BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
                                StringBuilder builder=new StringBuilder();
                                String temp;
                                while((temp=in.readLine())!=null)
                                {
                                    builder.append(temp);
                                }

                                if(in!=null)
                                {
                                    in.close();

                                }
                                if(conn!=null)
                                {
                                    conn.disconnect();
                                }

                                JSONObject jsonObject=new JSONObject(builder.toString());
                                if(jsonObject.getInt("count")>0)
                                {
                                    /*
                                    * 从index表中选择第一个，然后从网上下载
                                    * */
                                    JSONArray jsonArray=jsonObject.getJSONArray("result");
                                    JSONObject result=jsonArray.getJSONObject(0);
                                    URL lrcUrl=new URL(result.getString("lrc"));
                                    HttpURLConnection conn1=(HttpURLConnection)lrcUrl.openConnection();
                                    conn1.setConnectTimeout(3000);
                                    conn1.setRequestMethod("GET");
                                    if(conn1.getResponseCode()==200)
                                    {
                                        BufferedReader in2=new BufferedReader(new InputStreamReader(conn1.getInputStream(),"utf-8"));
                                        StringBuilder sb=new StringBuilder();
                                        String temp2;
                                        while((temp2=in2.readLine())!=null)
                                        {
                                            sb.append(temp2+"\n");
                                        }
                                        /*
                                        * 打开文件，写入文件中
                                        * */
                                        File file=new File(lrcName);
                                        if (!file.exists())
                                        {
                                            File lrcDir=new File(sdCard+StatusService.APP_ROOT_DIR+StatusService.LRC_PATH+"/");
                                            if(!lrcDir.exists())
                                            {
                                                lrcDir.mkdirs();
                                            }
                                            file.createNewFile();
                                        }
                                        FileOutputStream out=new FileOutputStream(file);
                                        BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(out));
                                        writer.write(sb.toString());
                                        if(writer!=null)
                                        {
                                            writer.close();
                                        }
                                        if(conn1!=null)
                                        {
                                            conn1.disconnect();
                                        }


                                    }
                                }

                            }



                        }catch (Exception e)
                        {
                            e.printStackTrace();
                            myNotifyAll(0,null);

                        }finally {

                        }

                    }
                    try
                    {
                        Thread.sleep(30);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    /*
                    * 所有操作进行完后，先等待一下，然后再去读取文件，如果仍然不存在，那么就说明失败
                    * */
                    lrc=new File(lrcName);
                    if(lrc.exists())
                    {
                        myNotifyAll(1,lrc);
                    }
                    else
                    {
                        myNotifyAll(0,null);
                    }


                }


            }
        }).start();


    }
}
