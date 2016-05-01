package com.example.zu.myapp.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import android.support.v4.content.LocalBroadcastManager;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.zu.myapp.R;
import com.example.zu.myapp.activity.PlayActivity;
import com.example.zu.myapp.model.Song;
import com.example.zu.myapp.util.LogUtil;
import com.example.zu.myapp.util.MyApplication;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zu on 2016/1/30.
 */
public class PlayService extends Service
{

    private boolean boot=true;
    private MediaPlayer mediaPlayer;
    private StatusService statusService=StatusService.getInstance();
//    private PlayBind playBind;
    private Song song;

    private NotificationCompat.Builder mBuilder;
    private RemoteViews remoteViews;
    LocalBroadcastManager localBroadcastManager;

    private Timer timer;
    private AudioManager audioManager;

    private  BroadcastReceiver activityBroadcastReceiver,notificationBroadcastReceiver;

    private BroadcastReceiver becomeNoiseReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY))
            {
                pause();
                sendMyBroadcast(StatusService.PLAY_PAUSE);
            }
        }
    };

//    private NotificationManager notificationManager;


    private void initMediaPlayer()
    {
        mediaPlayer=new MediaPlayer();


        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {

                sendMyBroadcast(StatusService.CHANGE_CURRENT_TIME);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNext();
                sendMyBroadcast(StatusService.CHANGE_PLAY_STATUS);
            }
        });
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        try
        {

            mediaPlayer.prepare();


        }catch (Exception e)
        {
            e.printStackTrace();
        }

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {

                sendMyBroadcast(StatusService.CHANGE_CURRENT_TIME);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNext();
                sendMyBroadcast(StatusService.CHANGE_PLAY_STATUS);
            }
        });

        AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener=new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange)
                {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        if(mediaPlayer!=null)
                        {
                            play();
                        }

                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:

                        if(mediaPlayer!=null)
                        {
                            pause();
                        }

                        break;
                    default:
                        break;

                }
            }
        };
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);


    }



    private void destroyMediaPlayer()
    {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer=null;


    }

    @Override
    public void onCreate() {

        super.onCreate();

        createNotification();
        setBroadcastReceiver();
        initMediaPlayer();

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        destroyMediaPlayer();
        localBroadcastManager.unregisterReceiver(activityBroadcastReceiver);
        unregisterReceiver(notificationBroadcastReceiver);
        unregisterReceiver(becomeNoiseReceiver);
        if(timer!=null)
        {
            timer.cancel();
        }
        statusService.saveStatus();



        LogUtil.v("PlayService","on destroy");

        super.onDestroy();

    }


    /*
        * 直接按下播放键的时候
        * */
    public void play()
    {
        if(boot==true)
        {
            song=statusService.getCurrent();
            try
            {
                mediaPlayer.setDataSource(song.getSongPath());
                mediaPlayer.prepareAsync();

            }catch (Exception e)
            {
                Toast.makeText(MyApplication.getContext(),"play fail",Toast.LENGTH_SHORT);
            }
            boot=false;
        }
        else
        {
            mediaPlayer.start();
        }


        updateNotification(StatusService.PLAY_PLAY);


    }

    /*
    * 当在songList中点击某一项的时候
    * */
    public void play(int position)
    {
        if(boot==true)
        {
            boot=false;
        }
        song=statusService.get(position);
        LogUtil.v("PlayService", song.getSongPath());
        try
        {
            mediaPlayer.stop();
            mediaPlayer.reset();

            mediaPlayer.setDataSource(song.getSongPath());
            mediaPlayer.prepareAsync();

        }catch (Exception e)
        {
            Toast.makeText(MyApplication.getContext(),"play fail",Toast.LENGTH_SHORT);
        }
        updateNotification(StatusService.PLAY_PLAY);

    }

    /*
    * 暂停播放
    * */
    public void pause()
    {
        mediaPlayer.pause();
        updateNotification(StatusService.PLAY_PAUSE);

    }

    /*
    * 播放下一首
    * */
    public void playNext()
    {
        LogUtil.v("PlayService broadcast", "play next");
        song=statusService.getNext();
        mediaPlayer.stop();
        mediaPlayer.reset();
        try
        {
            mediaPlayer.setDataSource(song.getSongPath());
            mediaPlayer.prepareAsync();
//                mediaPlayer.start();
        }catch (Exception e)
        {
            Toast.makeText(MyApplication.getContext(),"play fail",Toast.LENGTH_SHORT);
        }
        updateNotification(StatusService.PLAY_PLAY);


    }

    /*
    * 播放上一首
    * */
    public void playPrevious()
    {
        song=statusService.getPrevious();
        mediaPlayer.stop();
        mediaPlayer.reset();
        try
        {
            mediaPlayer.setDataSource(song.getSongPath());
            mediaPlayer.prepareAsync();
//                mediaPlayer.start();
        }catch (Exception e)
        {
            Toast.makeText(MyApplication.getContext(),"play fail",Toast.LENGTH_SHORT);
        }
        updateNotification(StatusService.PLAY_PLAY);

    }







    private void createNotification()
    {
        if(mBuilder==null)
        {
            mBuilder=new NotificationCompat.Builder(this);
            remoteViews=new RemoteViews(getPackageName(), R.layout.notification);

            Intent intent1=new Intent(StatusService.LIGHT_PLAYER_STATUS_INFO_TO_SERVICE);
            intent1.putExtra(StatusService.ACTION, StatusService.PLAY_PLAY);
            PendingIntent pendingIntent1=PendingIntent.getBroadcast(MyApplication.getContext(),1,intent1,0);
            remoteViews.setOnClickPendingIntent(R.id.notification_play_button, pendingIntent1);

            Intent intent2=new Intent(StatusService.LIGHT_PLAYER_STATUS_INFO_TO_SERVICE);
            intent2.putExtra(StatusService.ACTION, StatusService.PLAY_PREVIOUS);
            PendingIntent pendingIntent2=PendingIntent.getBroadcast(MyApplication.getContext(),2,intent2,0);
            remoteViews.setOnClickPendingIntent(R.id.notification_previous_button, pendingIntent2);

            Intent intent3=new Intent(StatusService.LIGHT_PLAYER_STATUS_INFO_TO_SERVICE);
            intent3.putExtra(StatusService.ACTION, StatusService.PLAY_NEXT);
            PendingIntent pendingIntent3=PendingIntent.getBroadcast(MyApplication.getContext(), 3, intent3, 0);
            remoteViews.setOnClickPendingIntent(R.id.notification_next_button, pendingIntent3);

            Intent intent4=new Intent(StatusService.LIGHT_PLAYER_STATUS_INFO_TO_SERVICE);
            intent4.putExtra(StatusService.ACTION, StatusService.EXIT);
            PendingIntent pendingIntent4=PendingIntent.getBroadcast(MyApplication.getContext(),4,intent4,0);
            remoteViews.setOnClickPendingIntent(R.id.notification_exit_button,pendingIntent4);



            Song current=statusService.getCurrent();
            remoteViews.setImageViewBitmap(R.id.notification_song_pic, statusService.getSongPic(current));
            remoteViews.setTextViewText(R.id.notification_song_name, current.getSongName());
            remoteViews.setTextViewText(R.id.notification_artist, current.getArtist());


            Intent intent=new Intent(MyApplication.getContext(), PlayActivity.class);
            TaskStackBuilder taskStackBuilder= TaskStackBuilder.create(MyApplication.getContext());
            taskStackBuilder.addParentStack(PlayActivity.class);
            taskStackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent=taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);

            mBuilder.setOngoing(true);
            mBuilder.setSmallIcon(R.drawable.default_pic).setTicker("Light Player is playing");
            Notification mNotification=mBuilder.build();
            mNotification.bigContentView=remoteViews;





//            notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            startForeground(1, mNotification);




        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(StatusService.ACTION,StatusService.PLAY_PLAY))
            {
                case StatusService.PLAY_PREVIOUS:
                    LogUtil.v("PlayService broadcast","StatusService.PLAY_PREVIOUS arrive");
                    playPrevious();
                    sendMyBroadcast(StatusService.PLAY_PREVIOUS);
                    LogUtil.v("PlayService broadcast", "send StatusService.PLAY_PREVIOUS");
                    break;
                case StatusService.PLAY_NEXT:
                    LogUtil.v("PlayService broadcast", "StatusService.PLAY_NEXT arrive");
                    playNext();
                    sendMyBroadcast(StatusService.PLAY_NEXT);
                    LogUtil.v("PlayService broadcast", "send StatusService.PLAY_NEXT");
                    break;

                case StatusService.PLAY_PLAY:

                    if(!mediaPlayer.isPlaying())
                    {
                        LogUtil.v("PlayService broadcast", "StatusService.PLAY_PLAY arrive");
                        play();
                        sendMyBroadcast(StatusService.PLAY_PLAY);
                        LogUtil.v("PlayService broadcast", "send StatusService.PLAY_PLAY");
                    }
                    else
                    {
                        LogUtil.v("PlayService broadcast", "StatusService.PLAY_PAUSE arrive");
                        pause();
                        sendMyBroadcast(StatusService.PLAY_PAUSE);
                        LogUtil.v("PlayService broadcast", "send StatusService.PLAY_PAUSE");
                    }
                    break;
                case StatusService.EXIT:
                    LogUtil.v("PlayService broadcast", "StatusService.EXIT arrive");
                    sendMyBroadcast(StatusService.EXIT);
                    stopSelf();
                    //finish
                    break;
                case StatusService.WANT_INFO:
                    LogUtil.v("PlayService broadcast", "StatusService.WANT_INFO arrive");
                    sendMyBroadcast(StatusService.WANT_INFO);
                    if(mediaPlayer.isPlaying())
                    {
                        sendMyBroadcast(StatusService.PLAY_PLAY);
                    }
                    else
                    {
                        sendMyBroadcast(StatusService.PLAY_PAUSE);
                    }

                    LogUtil.v("PlayService broadcast", "send StatusService.WANT_INFO");
                    break;
                case StatusService.CHANGE_PLAY_STATUS:
                    LogUtil.v("PlayService broadcast", "StatusService.CHANGE_PLAY_STATUS arrive");
                    int position=intent.getIntExtra(StatusService.SONG_INDEX,0);
                    play(position);
                    sendMyBroadcast(StatusService.CHANGE_PLAY_STATUS,1);
                    LogUtil.v("PlayService broadcast", "send StatusService.CHANGE_PLAY_STATUS");
                    break;
                case StatusService.CHANGE_CURRENT_TIME:
                    int time=intent.getIntExtra(StatusService.CURRENT_TIME,0);
                    if(mediaPlayer.isPlaying())
                    {
                        //mediaPlayer.stop();
                        mediaPlayer.seekTo(time);
                    }


                    break;
                case StatusService.CHANGE_REPEAT_MODE:
                    StatusService.REPEAT repeat=statusService.getRepeatMode();
                    repeat= StatusService.REPEAT.values()[(repeat.ordinal()+1)%3];
                    statusService.setRepeatMode(repeat);
                    sendMyBroadcast(StatusService.CHANGE_REPEAT_MODE);
                    break;

                case StatusService.TIMER:
                    int timerValue=intent.getIntExtra(StatusService.TIMER_VALUE,0);
                    if(timer==null)
                    {
                        timer=new Timer();

                    }
                    else
                    {
                        timer.cancel();
                        timer=new Timer();
                    }
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            pause();
                        }
                    },timerValue*60*1000);
                    break;

                default:

                    break;

            }
        }
    }



    private void setBroadcastReceiver()
    {

        activityBroadcastReceiver=new MyBroadcastReceiver();
        IntentFilter filter=new IntentFilter(StatusService.LIGHT_PLAYER_STATUS_INFO_TO_SERVICE);
        localBroadcastManager=LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(activityBroadcastReceiver, filter);
//        registerReceiver(activityBroadcastReceiver, filter);
        notificationBroadcastReceiver=new MyBroadcastReceiver();
        IntentFilter filter1=new IntentFilter(StatusService.LIGHT_PLAYER_STATUS_INFO_TO_SERVICE);
        registerReceiver(notificationBroadcastReceiver,filter1);

        IntentFilter filter2=new IntentFilter("android.media.AUDIO_BECOMING_NOISY");
        registerReceiver(becomeNoiseReceiver,filter2);


    }


    /*
    * 这里的status是播放还是暂停
    * */
    private void updateNotification(int... status)
    {
        mBuilder=new NotificationCompat.Builder(this);
        remoteViews=new RemoteViews(getPackageName(), R.layout.notification);

        Intent intent1=new Intent(StatusService.LIGHT_PLAYER_STATUS_INFO_TO_SERVICE);

        intent1.putExtra(StatusService.ACTION, StatusService.PLAY_PLAY);
        PendingIntent pendingIntent1=PendingIntent.getBroadcast(MyApplication.getContext(),1,intent1,0);
        remoteViews.setOnClickPendingIntent(R.id.notification_play_button, pendingIntent1);

        Intent intent2=new Intent(StatusService.LIGHT_PLAYER_STATUS_INFO_TO_SERVICE);
        intent2.putExtra(StatusService.ACTION, StatusService.PLAY_PREVIOUS);
        PendingIntent pendingIntent2=PendingIntent.getBroadcast(MyApplication.getContext(),2,intent2,0);
        remoteViews.setOnClickPendingIntent(R.id.notification_previous_button, pendingIntent2);

        Intent intent3=new Intent(StatusService.LIGHT_PLAYER_STATUS_INFO_TO_SERVICE);
        intent3.putExtra(StatusService.ACTION, StatusService.PLAY_NEXT);
        PendingIntent pendingIntent3=PendingIntent.getBroadcast(MyApplication.getContext(), 3, intent3, 0);
        remoteViews.setOnClickPendingIntent(R.id.notification_next_button, pendingIntent3);

        Song current=statusService.getCurrent();
        remoteViews.setImageViewBitmap(R.id.notification_song_pic, statusService.getSongPic(current));
        remoteViews.setTextViewText(R.id.notification_song_name, current.getSongName());
        remoteViews.setTextViewText(R.id.notification_artist, current.getArtist());

        if(status.length!=0)
        {
            if(status[0]==StatusService.PLAY_PLAY)
            {

                remoteViews.setImageViewResource(R.id.notification_play_button,R.drawable.notification_pause);
            }
            else if(status[0]==StatusService.PLAY_PAUSE)
            {

                remoteViews.setImageViewResource(R.id.notification_play_button,R.drawable.notification_play);
            }
        }


        Intent intent=new Intent(MyApplication.getContext(), PlayActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,0);
        mBuilder.setContentIntent(pendingIntent);

        mBuilder.setOngoing(true);
        mBuilder.setSmallIcon(R.drawable.default_pic).setTicker("Light Player is playing");
        Notification mNotification=mBuilder.build();
        mNotification.bigContentView=remoteViews;





//        notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        startForeground(1,mNotification);



    }

    /*
    * 这里的action是change_play_status,当播放完一首自动切换下一首的时候需要，通知其他的activity改变视图，第二个参数
    * 表示是否手动决定是否播放
    * */
    private void sendMyBroadcast(int... action)
    {
        Intent intent=new Intent(StatusService.LIGHT_PLAYER_STATUS_INFO_TO_ACTIVITY);
        if(action.length!=0)
        {
            intent.putExtra(StatusService.ACTION,action[0]);

        }
        if(action.length==2 && action[1]==1)
        {
            intent.putExtra(StatusService.IS_PLAYING,true);
        }
        else
        {
            intent.putExtra(StatusService.IS_PLAYING,mediaPlayer.isPlaying());
        }

        intent.putExtra(StatusService.SONG_INDEX,statusService.getNowPosition());

        intent.putExtra(StatusService.CURRENT_TIME,mediaPlayer.getCurrentPosition());

        localBroadcastManager.sendBroadcast(intent);
//        sendBroadcast(intent);
    }



}

/*
* 交互太混乱了，考虑使用广播。如果继续使用Bind，只能使用回调了。但是使用广播，服务无法知道何时该发送何时该停止，
* 如果一直保持发送，那么会造成很大负担。
* */


