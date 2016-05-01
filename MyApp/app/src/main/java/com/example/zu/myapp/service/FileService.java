package com.example.zu.myapp.service;

import android.content.Context;

import com.example.zu.myapp.model.Song;
import com.example.zu.myapp.util.FileUtil;
import com.example.zu.myapp.util.MyApplication;

import java.util.List;

/**
 * Created by zu on 2016/1/27.
 */


public class FileService
{



    public static List<Song> loadSongs()
    {
        return FileUtil.loadFromMediaStore(MyApplication.getContext());
    }

    public static void deleteSong(Song song)
    {
        FileUtil.deleteSong(song);
    }

    public static void updateSong(Song song)
    {
        FileUtil.updateSong(song);
    }

}
