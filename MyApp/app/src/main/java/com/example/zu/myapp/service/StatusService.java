package com.example.zu.myapp.service;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.LruCache;

import com.example.zu.myapp.R;
import com.example.zu.myapp.model.Song;
import com.example.zu.myapp.util.MyApplication;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


/**
 * Created by zu on 2016/1/27.
 */
public class StatusService
{
    public enum REPEAT{ALL,ONE,RANDOM};
    private REPEAT repeatMode=REPEAT.ALL;
    

    
    private Song nowPlaying;
    private int nowPosition=0;
    private List<Song> songs=null;
    private LruCache<Long,Bitmap> songPics=new LruCache<Long,Bitmap>(10)
    {
        @Override
        protected void entryRemoved(boolean evicted, Long key, Bitmap oldValue, Bitmap newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
        }
    };
    ArrayList<HashMap<String,String>> songList=new ArrayList<>();
    private static StatusService statusService;

    public static final String LRC_PATH="/Lyric";
    public static final String APP_ROOT_DIR="/LightPlayer";
    public static final String LIGHT_PLAYER_STATUS_INFO_TO_ACTIVITY="com.zu.MyApp.status_info_to_activity";
    public static final String LIGHT_PLAYER_WANT_INFO="com.zu.MyApp.want_info";
    public static final int PLAY_PREVIOUS=0;
    public static final int PLAY_NEXT=1;
    public static final int PLAY_PAUSE=2;
    public static final int PLAY_PLAY=3;
    public static final int EXIT=4;
    public static final int CHANGE_CURRENT_TIME=5;
    public static final int CHANGE_REPEAT_MODE=6;
    public static final String REPEAT_MODE="repeat_mode";
    public static final String ACTION="action";

    public static final int WANT_INFO=7;
    public static final int TIMER=8;
    public static final String TIMER_VALUE="timer_value";
    public static final String SONG_INDEX="song_index";
    public static final String CURRENT_TIME="current_time";
    public static final String IS_PLAYING="is_playing";
    public static final int CHANGE_PLAY_STATUS=11;
    public static final String LIGHT_PLAYER_STATUS_INFO_TO_SERVICE="com.zu.MyApp.status_info_to_service";


    private StatusService()
    {
        //从文件中读取配置
        initStatus();
    }

    public void loadStatus()
    {
        SharedPreferences sharedPreferences=MyApplication.getContext().getSharedPreferences("status", Context.MODE_MULTI_PROCESS);

        nowPosition=sharedPreferences.getInt("nowPosition", 0);
        repeatMode=REPEAT.values()[sharedPreferences.getInt("repeatMode",0)];

    }

    public void saveStatus()
    {
        SharedPreferences sharedPreferences=MyApplication.getContext().getSharedPreferences("status",Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt("nowPosition",nowPosition);
        editor.putInt("repeatMode",repeatMode.ordinal());
        editor.commit();

    }

    public static StatusService getInstance()
    {
        if(statusService==null)
        {
            statusService=new StatusService();
        }
        return statusService;
    }

    public REPEAT getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(REPEAT repeatMode) {
        this.repeatMode = repeatMode;
    }







    public List<Song> getSongs() {

        return songs;
    }

    /*
    * 获得某一特定歌曲的方法，同时改变当前正在播放
    * */
    public Song get(int position)
    {
        nowPosition=position;
        nowPlaying=songs.get(nowPosition);



        return nowPlaying;
    }

    public Song getCurrent()
    {
        return get(nowPosition);
    }

    public Song getNext()
    {
        nowPosition=getNextPosition();

        return get(nowPosition);
    }

    public Song getPrevious()
    {
        nowPosition=getPreviousPosition();

        return get(nowPosition);
    }

    private int getNextPosition()
    {
        int temp;
        switch (repeatMode)
        {
            case ALL:
                temp=nowPosition+1;
                if(temp>=songs.size())
                {
                    temp=0;
                }
                return temp;

            case ONE:
                return nowPosition;

            case RANDOM:
                Random random=new Random(System.currentTimeMillis());
                temp=random.nextInt(songs.size()-1);
                return temp;
            default:
                return 0;

        }
    }

    private int getPreviousPosition()
    {
        int temp;
        switch (repeatMode)
        {
            case ALL:
                temp=nowPosition-1;
                if(temp<0)
                {
                    temp=songs.size()-1;
                }
                return temp;

            case ONE:
                return nowPosition;

            case RANDOM:
                Random random=new Random(System.currentTimeMillis());
                temp=random.nextInt(songs.size()-1);
                return temp;
            default:
                return 0;

        }
    }





    public void initStatus()
    {
        songs=FileService.loadSongs();
        loadStatus();
        nowPlaying=songs.get(nowPosition);

    }

    public ArrayList<HashMap<String,String>> getSongList()
    {
        if(songList.size()==0)
        {
            for(Song song : songs)
            {
                HashMap<String,String> map=new HashMap<>();
                map.put("song_name",song.getSongName());
                map.put("artist",song.getArtist());

                int duration=song.getSongDuration();
                String s=""+duration/(10*60*1000)+""+(duration/(60*1000))%10+":"+duration/(1000)%60/10+""+duration/1000%60%10;


                map.put("song_duration", s);
                songList.add(map);
            }
        }
        return songList;
    }

    public int getNowPosition()
    {
        return nowPosition;
    }

    public Bitmap getSongPic(Song song)
    {
        if(songPics.get(song.getId())==null)
        {
            if(nowPlaying.getImage()==null)
            {
                songPics.put(song.getId(),ArtWorkUtil.getArtwork(MyApplication.getContext(),null,song.getId(),song.getAlbumId(),true));

            }
        }
        return songPics.get(song.getId());
    }





}


class ArtWorkUtil
{
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();

    public static Bitmap getArtwork(Context context, String title, long song_id, long album_id,
                                    boolean allowDefault) {
        //如果album_id不可以就用song_id,  getArtworkFromFile,如果都不可用就获取默认图片
        if (album_id < 0) {
            if (song_id >= 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            if (allowDefault) {
                return getDefaultArtwork(context);
            }
            return null;
        }

        //如果album_id可用
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            //用InputStream获取文件流
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(in, null, sBitmapOptions);
                if (bmp == null) {
                    bmp = getDefaultArtwork(context);
                }
                return bmp;
            } catch (FileNotFoundException ex) {
                //如果album_id的方法找不到文件，就使用song_id,getArtworkFromFile
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowDefault) {
                            return getDefaultArtwork(context);
                        }
                    }
                } else if (allowDefault) {
                    bm = getDefaultArtwork(context);
                }
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static Bitmap getArtworkFromFile(Context context, long song_id, long album_id) {
        Bitmap bm = null;
        if (album_id < 0 && song_id < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            if (album_id < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + song_id + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver()
                        .openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
                ParcelFileDescriptor pfd = context.getContentResolver()
                        .openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            }
        } catch (FileNotFoundException ex) {

        }
        return bm;
    }

    private static Bitmap getDefaultArtwork(Context context) {


        Bitmap bmp=BitmapFactory.decodeResource(context.getResources(),R.drawable.default_pic);
        Bitmap newBmp=Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(newBmp);
        Paint paint=new Paint();
        canvas.drawBitmap(bmp,0,0,paint);
        return newBmp;
    }
}



