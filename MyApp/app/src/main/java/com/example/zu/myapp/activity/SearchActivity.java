package com.example.zu.myapp.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.zu.myapp.R;
import com.example.zu.myapp.model.Song;
import com.example.zu.myapp.service.StatusService;
import com.example.zu.myapp.util.SearchUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends Activity implements View.OnClickListener{

    private EditText searchSongName;
    private ImageButton searchButton;
    private ListView searchResult;
    private ImageView statusBar;
    private StatusService statusService=StatusService.getInstance();
    LocalBroadcastManager broadcastManager=LocalBroadcastManager.getInstance(this);
    ArrayList<Song> songs=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initComponent();



    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void initComponent()
    {
        searchSongName=(EditText)findViewById(R.id.search_text);
        searchButton=(ImageButton)findViewById(R.id.search_button_in_search);
        searchButton.setOnClickListener(this);
        searchResult=(ListView)findViewById(R.id.search_result_list);
        searchResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song song=songs.get(position);
                ArrayList<Song> songs1=(ArrayList<Song>)statusService.getSongs();
                int i=songs1.indexOf(song);
                Intent intent=new Intent(StatusService.LIGHT_PLAYER_STATUS_INFO_TO_SERVICE);
                intent.putExtra(StatusService.ACTION,StatusService.CHANGE_PLAY_STATUS);
                intent.putExtra(StatusService.SONG_INDEX,i);
                broadcastManager.sendBroadcast(intent);
                SearchActivity.this.finish();
            }
        });

        statusBar=(ImageView)findViewById(R.id.search_status_bar);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            ViewGroup.LayoutParams params=statusBar.getLayoutParams();
            params.height=getStatusBarHeight();
            statusBar.setLayoutParams(params);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.search_button_in_search:
                String arg=searchSongName.getText().toString();
                songs= SearchUtil.search(arg);
                ArrayList<HashMap<String,String>> songList=new ArrayList<>();
                for(Song song:songs)
                {
                    HashMap<String,String> map=new HashMap<>();
                    map.put("song_name",song.getSongName());
                    map.put("artist",song.getArtist());
                    int duration=song.getSongDuration();
                    String s=""+duration/(10*60*1000)+""+(duration/(60*1000))%10+":"+duration/(1000)%60/10+""+duration/1000%60%10;
                    map.put("song_duration",s);
                    songList.add(map);
                }
                SimpleAdapter adapter=new SimpleAdapter(SearchActivity.this,songList,R.layout.song_list_item,
                        new String[]{"song_name","artist","song_duration"},new int[]{R.id.song_name_in_list,R.id.artist_in_list,R.id.song_duration});
                searchResult.setAdapter(adapter);
        }
    }
}
