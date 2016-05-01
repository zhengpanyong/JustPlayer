package com.example.zu.myapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.IBinder;

import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zu.myapp.R;
import com.example.zu.myapp.layout.SlideLayout;
import com.example.zu.myapp.model.Song;
import com.example.zu.myapp.service.PlayService;
import com.example.zu.myapp.service.StatusService;
import com.example.zu.myapp.util.LogUtil;
import com.example.zu.myapp.util.MyApplication;

import java.util.ArrayList;
import java.util.FormatFlagsConversionMismatchException;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener
{
    private SlideLayout slideLayout;
    private ImageButton searchButton;
    private ImageButton switchButton;
    private TextView title;//顶部的标题，显示当前是所有音乐还是按歌手
    private ImageButton previousButton;
    private ImageButton playButton;
    private ImageButton nextButton;
    private ListView songList;
    private boolean isPlaying=false;

    private LinearLayout leftMenuExit;
    private LinearLayout repeatMode;
    private LinearLayout timerInLeft;

    private ImageView repeatModeImage;
    private TextView repeatModeText;


    private StatusService statusService=StatusService.getInstance();
    private LocalBroadcastManager localBroadcastManager;
    StatusBroadcastReceiver statusBroadcastReceiver;

    private TextView songName;
    private TextView artist;
    private ImageView songPic;
    private LinearLayout infoBar;
    private ImageView leftMenuSongPic;
    private ViewGroup titleBar;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtil.v("MainActivity", "onCreate()");
        setContentView(R.layout.activity_main);




        //初始化各组件，歌曲列表必须在组件初始化之前得到，因为ListView需要这个数据
        initComponent();
        //如果SDK在4.4以上，就开启透明状态栏
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            ViewGroup.MarginLayoutParams titleBarParams=(ViewGroup.MarginLayoutParams)titleBar.getLayoutParams();
            titleBarParams.height=titleBarParams.height+getStatusBarHeight();
            titleBar.setLayoutParams(titleBarParams);
            titleBar.setPadding(0,getStatusBarHeight(),0,0);
        }



        setBroadcastReceiver();

        /*
        * 启动PlayService
        * */
        Intent playIntent=new Intent(MainActivity.this,PlayService.class);
        startService(playIntent);
        title.setText(R.string.all_music);

        updateInfoBar();
        updateRepeatMode();




    }

    @Override
    protected void onStart() {
        super.onStart();
        setBroadcastReceiver();

        /*
        * 之所以要在开启服务之后又要bind，是因为这样可以使服务脱离于创建它的activity运行，这样的话，不管哪个activity，
        * 要控制播放只要bind一下就可以了，而不至于activity一死就服务也停止
        * */
        /*Intent intent=new Intent(MainActivity.this,PlayService.class);
        bindService(intent, myConnection, BIND_AUTO_CREATE);
        LogUtil.v("MainActivity","onStart()");*/


    }

    @Override
    protected void onResume() {
        super.onResume();
        sendMyBroadcast(StatusService.WANT_INFO);
        LogUtil.v("MainActivity", "send StatusService.WANT_INFO");
        LogUtil.v("MainActivity", "onResume()");



    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.v("MainActivity","onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        localBroadcastManager.unregisterReceiver(statusBroadcastReceiver);
        LogUtil.v("MainActivity", "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        broadcastManager.unregisterReceiver(statusBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(statusBroadcastReceiver);
        /*unbindService(myConnection);
        LogUtil.v("MainActivity", "onDestroy()");*/
    }

    private void setBroadcastReceiver()
    {
        IntentFilter intentFilter=new IntentFilter(StatusService.LIGHT_PLAYER_STATUS_INFO_TO_ACTIVITY);
        statusBroadcastReceiver=new StatusBroadcastReceiver();
        localBroadcastManager=LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(statusBroadcastReceiver, intentFilter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.main_search_button:
                Intent intent1=new Intent(this,SearchActivity.class);
                startActivity(intent1);
                break;
            /*
            * 发送时全部都发为PLAY，然后PlayService根据MediaPlayer的状态决定是播放还是暂停，并且将状态反馈给Activity来更新
            * 视图。
            * */
            case R.id.play_button:
                sendMyBroadcast(StatusService.PLAY_PLAY);
                LogUtil.v("MainActivity", "send StatusService.PLAY_PLAY");

                break;
            case R.id.previous_button:

                playButton.setImageResource(R.drawable.pause);
                sendMyBroadcast(StatusService.PLAY_PREVIOUS);
                LogUtil.v("MainActivity", "send StatusService.PLAY_PREVIOUS");
                break;
            case R.id.next_button:
//                playBind.playNext();
                playButton.setImageResource(R.drawable.pause);
                sendMyBroadcast(StatusService.PLAY_NEXT);
                LogUtil.v("MainActivity", "send StatusService.PLAY_NEXT");
                break;
            case R.id.info_bar:
                Intent intent=new Intent(this,PlayActivity.class);
                startActivity(intent);
                break;
            case R.id.switch_button:
                slideLayout.scrollToRight();
                break;
            case R.id.exit_in_left_menu:
                sendMyBroadcast(StatusService.EXIT);
                break;

            case R.id.repeat_mode_in_left_menu:
                sendMyBroadcast(StatusService.CHANGE_REPEAT_MODE);
                break;
            case R.id.timer_in_left_menu:
                createDialog();
                break;
            default:
                break;

        }
    }

    private void initComponent()
    {
        slideLayout=(SlideLayout)findViewById(R.id.slide_layout);

        title=(TextView)findViewById(R.id.title);

        searchButton=(ImageButton)findViewById(R.id.main_search_button);
        searchButton.setOnClickListener(this);

        playButton=(ImageButton)findViewById(R.id.play_button);
        playButton.setOnClickListener(this);



        previousButton=(ImageButton)findViewById(R.id.previous_button);
        previousButton.setOnClickListener(this);

        nextButton=(ImageButton)findViewById(R.id.next_button);
        nextButton.setOnClickListener(this);

        songName=(TextView)findViewById(R.id.song_name);
        artist=(TextView)findViewById(R.id.artist);
        songPic=(ImageView)findViewById(R.id.song_pic);

        infoBar=(LinearLayout)findViewById(R.id.info_bar);
        infoBar.setOnClickListener(this);

        switchButton=(ImageButton)findViewById(R.id.switch_button);
        switchButton.setOnClickListener(this);

        leftMenuExit=(LinearLayout)findViewById(R.id.exit_in_left_menu);
        leftMenuExit.setOnClickListener(this);

        leftMenuSongPic=(ImageView)findViewById(R.id.left_menu_song_pic);

        titleBar=(ViewGroup)findViewById(R.id.title_bar);

        /*
        * 生成ListView视图
        * */
        songList=(ListView)findViewById(R.id.song_list);


        ArrayList<HashMap<String,String>> list=statusService.getSongList();

        SimpleAdapter adapter=new SimpleAdapter(MainActivity.this,list,R.layout.song_list_item,
                new String[]{"song_name","artist","song_duration"},new int[]{R.id.song_name_in_list,R.id.artist_in_list,R.id.song_duration});
        songList.setAdapter(adapter);
        songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                playBind.play(position);
                playButton.setImageResource(R.drawable.pause);
                updateInfoBar();
                sendMyBroadcast(StatusService.CHANGE_PLAY_STATUS, position);
                LogUtil.v("MainActivity", "send StatusService.CHANGE_PLAY_STATUS");
            }
        });

        repeatMode=(LinearLayout)findViewById(R.id.repeat_mode_in_left_menu);
        repeatMode.setOnClickListener(this);

        repeatModeImage=(ImageView)findViewById(R.id.repeat_mode_image);
        repeatModeText=(TextView)findViewById(R.id.repeat_mode_text);

        timerInLeft=(LinearLayout)findViewById(R.id.timer_in_left_menu);
        timerInLeft.setOnClickListener(this);
    }

    private void createDialog()
    {
        final EditText editText=new EditText(this);
        

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("定时关闭(分钟)");
        builder.setView(editText);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    int time = Integer.parseInt(editText.getText().toString());
                    sendMyBroadcast(StatusService.TIMER, time);
                } catch (FormatFlagsConversionMismatchException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "请输入整数", Toast.LENGTH_SHORT);
                }
                dialog.dismiss();

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
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

    /*
    * 得到用于生成ListView视图的列表资源
    * */


    /*
    * 更新底部的歌曲信息栏
    * */
    private void updateInfoBar()
    {
        Song playing=statusService.getCurrent();
        songName.setText(playing.getSongName());
        artist.setText(playing.getArtist());
        Bitmap songPicTemp=statusService.getSongPic(playing);
        songPic.setImageBitmap(songPicTemp);
        leftMenuSongPic.setImageBitmap(songPicTemp);
        //Toast.makeText(this,"歌曲路径："+playing.getSongPath(),Toast.LENGTH_SHORT).show();
    }

    private class StatusBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(StatusService.ACTION,StatusService.PLAY_PLAY))
            {
                case StatusService.PLAY_PLAY:
                    LogUtil.v("MainActivity","StatusService.PLAY_PLAY arrive");
                    playButton.setImageResource(R.drawable.pause);
                    updateInfoBar();
                    break;
                case StatusService.PLAY_PAUSE:
                    LogUtil.v("MainActivity","StatusService.PLAY_PAUSE arrive");
                    playButton.setImageResource(R.drawable.play);
                    break;
                case StatusService.PLAY_NEXT:
                    LogUtil.v("MainActivity","StatusService.PLAY_NEXT arrive");
                    playButton.setImageResource(R.drawable.pause);
                    updateInfoBar();
                    break;
                case StatusService.PLAY_PREVIOUS:
                    LogUtil.v("MainActivity","StatusService.PLAY_PREVIOUS arrive");
                    playButton.setImageResource(R.drawable.pause);
                    updateInfoBar();
                    break;
                case StatusService.CHANGE_PLAY_STATUS:
                    LogUtil.v("MainActivity","StatusService.CHANGE_PLAY_STATUS arrive");
                    updateInfoBar();
                    isPlaying=intent.getBooleanExtra(StatusService.IS_PLAYING, false);
                    //Toast.makeText(MainActivity.this,"isPlaying is "+isPlaying,Toast.LENGTH_SHORT).show();
                    if(isPlaying)
                    {

                        playButton.setImageResource(R.drawable.pause);
                    }
                    else
                    {
                        playButton.setImageResource(R.drawable.play);
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
        StatusService.REPEAT repeat=statusService.getRepeatMode();
        switch (repeat)
        {
            case ALL:
                repeatModeImage.setImageResource(R.drawable.repeat_all_black);
                repeatModeText.setText("全部循环");
                break;
            case ONE:
                repeatModeImage.setImageResource(R.drawable.repeat_one_black);
                repeatModeText.setText("单曲循环");
                break;
            case RANDOM:
                repeatModeImage.setImageResource(R.drawable.repeat_random_black);
                repeatModeText.setText("随机播放");
                break;
            default:
                break;
        }
    }

    private void sendMyBroadcast(int action,int... content)
    {
        Intent intent=new Intent(StatusService.LIGHT_PLAYER_STATUS_INFO_TO_SERVICE);
        intent.putExtra(StatusService.ACTION, action);
        if(action==StatusService.CHANGE_PLAY_STATUS)
        {
            intent.putExtra(StatusService.SONG_INDEX,content[0]);

        }
        if(action==StatusService.TIMER)
        {
            intent.putExtra(StatusService.TIMER_VALUE,content[0]);
        }
        localBroadcastManager.sendBroadcast(intent);
//        sendBroadcast(intent);
    }
}

