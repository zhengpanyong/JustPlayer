package com.example.zu.myapp.activity;

import android.app.Activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.AsyncTask;
import android.os.Build;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zu.myapp.R;
import com.example.zu.myapp.fragment.ArtworkFragment;
import com.example.zu.myapp.fragment.LrcFragment;
import com.example.zu.myapp.model.Song;
import com.example.zu.myapp.service.StatusService;
import com.example.zu.myapp.util.BitmapBlur;
import com.example.zu.myapp.util.FileUtil;
import com.example.zu.myapp.util.LogUtil;
import com.example.zu.myapp.util.LrcUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/*
* TODO:设置seek bar，broadcast
* */
public class PlayActivity extends FragmentActivity implements View.OnClickListener{

    private ImageView statusBar;
    private ImageButton playActivityBack;
    private ImageButton playActivityNext;
    private ImageButton playActivityPlay;
    private ImageButton playActivityPrevious;
    private ImageButton playActivityRepeatImage;
    private SeekBar playActivitySeekBar;

    private ArtworkFragment artworkFragment;
    private LrcFragment lrcFragment;

    private TextView playActivitySongName;
    private TextView playActivityArtist;
    private TextView playActivitySongDuration;
    private TextView playActivityNowPosition;
    private ImageView playActivityBackground;
    private LinearLayout playActivityLayout;

    private ViewPager viewPager;

    private ArrayList<Fragment> fragments;

    private StatusService statusService=StatusService.getInstance();

    private StatusBroadcastReceiver statusBroadcastReceiver;

    private LocalBroadcastManager localBroadcastManager;
    private Song nowPlaying;

    private Timer timer;

    private FileUtil fileUtil=new FileUtil();
    private LrcUtil lrcUtil=LrcUtil.getInstance();
    private boolean firstBoot=true;

    private FileUtil.OnLrcCompleteListener myListener=new FileUtil.OnLrcCompleteListener() {
        @Override
        public void onComplete(int resultCode, File lrcFile) {
            if(resultCode!=0)
            {
                lrcFragment.setData(lrcUtil.getLrc(lrcFile));
            }
            else
            {
                TreeMap<Integer,String> temp=new TreeMap<Integer, String>();
                temp.put(0,"没有找到歌词");
                lrcFragment.setData(temp);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        statusBar=(ImageView)findViewById(R.id.status_bar);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            ViewGroup.LayoutParams params=statusBar.getLayoutParams();
            params.height=getStatusBarHeight();
            statusBar.setLayoutParams(params);
        }

        initComponent();

        updateRepeatMode();

        fragments=new ArrayList<>();
        artworkFragment=new ArtworkFragment();
        fragments.add(artworkFragment);
        lrcFragment=new LrcFragment();

        fragments.add(lrcFragment);

        viewPager.setAdapter(new MyFragmentAdapter(getSupportFragmentManager(), fragments));



        firstBoot=true;
        fileUtil.setOnLrcCompleteListener(myListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        setBroadcast();
    }

    @Override
    protected void onResume() {
        super.onResume();
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendMyBroadcast(StatusService.WANT_INFO);
            }
        }, 300, 200);







    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
        localBroadcastManager.unregisterReceiver(statusBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fileUtil.removeOnHttpLrcCompleteListener(myListener);
    }

    /*
            * 设置广播
            * */
    private void setBroadcast()
    {
        IntentFilter intentFilter=new IntentFilter(StatusService.LIGHT_PLAYER_STATUS_INFO_TO_ACTIVITY);
        statusBroadcastReceiver=new StatusBroadcastReceiver();
        localBroadcastManager=LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(statusBroadcastReceiver, intentFilter);
    }



    /*
    * 初始化控件
    * */
    private void initComponent()
    {
        playActivityBack=(ImageButton)findViewById(R.id.play_activity_back);
        playActivityBack.setOnClickListener(this);

        playActivityNext=(ImageButton)findViewById(R.id.play_activity_next_button);
        playActivityNext.setOnClickListener(this);

        playActivityPrevious=(ImageButton)findViewById(R.id.play_activity_previous_button);
        playActivityPrevious.setOnClickListener(this);

        playActivityPlay=(ImageButton)findViewById(R.id.play_activity_play_button);
        playActivityPlay.setOnClickListener(this);

        playActivitySeekBar=(SeekBar)findViewById(R.id.duration_seek_bar);
        playActivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int time = seekBar.getProgress() * nowPlaying.getSongDuration() / 100;

                //Toast.makeText(PlayActivity.this, "seek time " + time + ",歌曲长度为：" + nowPlaying.getSongDuration(), Toast.LENGTH_LONG).show();
                sendMyBroadcast(StatusService.CHANGE_CURRENT_TIME, time);
            }
        });

        playActivitySeekBar.setMax(100);
        playActivitySeekBar.setIndeterminate(false);

        playActivityBackground=(ImageView)findViewById(R.id.play_activity_background);

        playActivityLayout=(LinearLayout)findViewById(R.id.play_activity_layout);



        playActivitySongName=(TextView)findViewById(R.id.play_activity_song_name);

        playActivityArtist=(TextView)findViewById(R.id.play_activity_artist);

        playActivitySongDuration=(TextView)findViewById(R.id.play_activity_song_duration);

        playActivityNowPosition=(TextView)findViewById(R.id.play_activity_now_position);

        viewPager=(ViewPager)findViewById(R.id.play_activity_view_pager);

        playActivityRepeatImage=(ImageButton)findViewById(R.id.play_activity_repeat_mode);
        playActivityRepeatImage.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.play_activity_next_button:
                sendMyBroadcast(StatusService.PLAY_NEXT);
                break;
            case R.id.play_activity_play_button:
                sendMyBroadcast(StatusService.PLAY_PLAY);
                break;
            case R.id.play_activity_previous_button:
                sendMyBroadcast(StatusService.PLAY_PREVIOUS);
                break;
            case R.id.play_activity_back:
                finish();
                break;
            case R.id.play_activity_repeat_mode:
                sendMyBroadcast(StatusService.CHANGE_REPEAT_MODE);
                break;
            default:
                break;
        }
    }

    /*
    * 得到状态栏高度
    * */
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void updateInfo()
    {
        nowPlaying=statusService.getCurrent();
        playActivitySongName.setText(nowPlaying.getSongName());
        playActivityArtist.setText(nowPlaying.getArtist());
        int duration=nowPlaying.getSongDuration();
        String s=""+duration/(10*60*1000)+""+(duration/(60*1000))%10+":"+duration/(1000)%60/10+""+duration/1000%60%10;
        playActivitySongDuration.setText(s);
        artworkFragment.setSongPic(statusService.getSongPic(nowPlaying));

    }

    private class StatusBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPlaying=false;
            int time=0;
            switch (intent.getIntExtra(StatusService.ACTION,StatusService.PLAY_PLAY))
            {
                case StatusService.PLAY_PLAY:
                    //LogUtil.v("MainActivity", "StatusService.PLAY_PLAY arrive");
                    playActivityPlay.setImageResource(R.drawable.play_activity_pause);
                    updateInfo();
                    break;
                case StatusService.PLAY_PAUSE:
                    //LogUtil.v("MainActivity","StatusService.PLAY_PAUSE arrive");
                    playActivityPlay.setImageResource(R.drawable.play_activity_play);
                    break;
                case StatusService.PLAY_NEXT:
                    //LogUtil.v("MainActivity","StatusService.PLAY_NEXT arrive");
                    playActivityPlay.setImageResource(R.drawable.play_activity_pause);
                    updateInfo();
                    setLrc();
                    setBackground();
                    break;
                case StatusService.PLAY_PREVIOUS:
                    //LogUtil.v("MainActivity","StatusService.PLAY_PREVIOUS arrive");
                    playActivityPlay.setImageResource(R.drawable.play_activity_pause);
                    updateInfo();
                    setLrc();
                    setBackground();
                    break;
                case StatusService.CHANGE_PLAY_STATUS:
                    //LogUtil.v("MainActivity","StatusService.CHANGE_PLAY_STATUS arrive");
                    updateInfo();
                    isPlaying=intent.getBooleanExtra(StatusService.IS_PLAYING, false);
                    //Toast.makeText(MainActivity.this,"isPlaying is "+isPlaying,Toast.LENGTH_SHORT).show();
                    if(isPlaying)
                    {

                        playActivityPlay.setImageResource(R.drawable.play_activity_pause);
                    }
                    else
                    {
                        playActivityPlay.setImageResource(R.drawable.play_activity_play);
                    }
                    setBackground();
                    break;
                case StatusService.CHANGE_CURRENT_TIME:
                    time=intent.getIntExtra(StatusService.CURRENT_TIME, 0);
                    nowPlaying=statusService.getCurrent();

                    playActivitySeekBar.setProgress(time * 100 / nowPlaying.getSongDuration());
                    String s=""+time/(10*60*1000)+""+(time/(60*1000))%10+":"+time/(1000)%60/10+""+time/1000%60%10;
                    playActivityNowPosition.setText(s);
                    lrcFragment.setTime(time/10);
                    break;
                case StatusService.WANT_INFO:

                    isPlaying=intent.getBooleanExtra(StatusService.IS_PLAYING, false);
                    //Toast.makeText(MainActivity.this,"isPlaying is "+isPlaying,Toast.LENGTH_SHORT).show();
                    if(isPlaying)
                    {

                        playActivityPlay.setImageResource(R.drawable.play_activity_pause);
                    }
                    else
                    {
                        playActivityPlay.setImageResource(R.drawable.play_activity_play);
                    }
                    time = intent.getIntExtra(StatusService.CURRENT_TIME, 0);
                    nowPlaying=statusService.getCurrent();

                    playActivitySeekBar.setProgress(time * 100 / nowPlaying.getSongDuration());
                    String s1=""+time/(10*60*1000)+""+(time/(60*1000))%10+":"+time/(1000)%60/10+""+time/1000%60%10;
                    playActivityNowPosition.setText(s1);
                    lrcFragment.setTime(time/10);
                    nowPlaying=statusService.getCurrent();
                    updateInfo();
                    if(firstBoot==true)
                    {
                        lrcFragment.init();
                        setLrc();
                        setBackground();
                        firstBoot=false;

                    }
                    break;
                case StatusService.EXIT:
                    finish();
                    break;
                case StatusService.CHANGE_REPEAT_MODE:
                    updateRepeatMode();
                    break;
                default:
                    break;
            }

        }
    }

    private void updateRepeatMode()
    {
        StatusService.REPEAT repeat= statusService.getRepeatMode();
        switch (repeat)
        {
            case ALL:
                playActivityRepeatImage.setImageResource(R.drawable.repeat_all_white);
                break;
            case ONE:
                playActivityRepeatImage.setImageResource(R.drawable.repeat_one_white);
                break;
            case RANDOM:
                playActivityRepeatImage.setImageResource(R.drawable.repeat_random_white);
                break;
            default:
                break;
        }
    }

    private void sendMyBroadcast(int... action)
    {
        LogUtil.v("PLAYACTIVITY1","send broadcast:"+action[0]);
        Intent intent=new Intent(StatusService.LIGHT_PLAYER_STATUS_INFO_TO_SERVICE);
        if(action.length!=0)
        {
            intent.putExtra(StatusService.ACTION,action[0]);
            if(action[0]==StatusService.CHANGE_CURRENT_TIME)
            {
                intent.putExtra(StatusService.CURRENT_TIME,action[1]);
            }
        }
        localBroadcastManager.sendBroadcast(intent);
    }

    private void setLrc()
    {
        TreeMap<Integer,String> temp=new TreeMap<>();
        temp.put(0,"正在搜索歌词");
        lrcFragment.setData(temp);

        nowPlaying=statusService.getCurrent();
        fileUtil.getLrc(nowPlaying.getSongName(), nowPlaying.getArtist());
    }

    private void setBackground()
    {
        nowPlaying=statusService.getCurrent();
        AsyncTask<Bitmap,Integer,Bitmap> myAsyncTask=new AsyncTask<Bitmap, Integer, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Bitmap... params) {
                return BitmapBlur.blur(params[0]);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                playActivityBackground.setImageBitmap(bitmap);
            }
        };
        myAsyncTask.execute(statusService.getSongPic(nowPlaying));

    }




}

class MyFragmentAdapter extends FragmentPagerAdapter
{
    private ArrayList<Fragment> fragments;

    public MyFragmentAdapter(FragmentManager manager,ArrayList<Fragment> fragments)
    {
        super(manager);
        this.fragments=fragments;
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
