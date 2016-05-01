package com.example.zu.myapp.util;

import com.example.zu.myapp.model.Song;
import com.example.zu.myapp.service.StatusService;

import java.util.ArrayList;

/**
 * Created by zu on 2016/4/29.
 */
public class SearchUtil
{
    private static StatusService statusService=StatusService.getInstance();
    private static ArrayList<Song> songs=(ArrayList)statusService.getSongs();

    public static ArrayList<Song> search(String arg)
    {
        ArrayList<Song> temp=new ArrayList<>();
        for(Song song:songs)
        {
            if(song.getSongName().toLowerCase().contains(arg.toLowerCase())||song.getArtist().toLowerCase().contains(arg.toLowerCase()))
            {
                temp.add(song);
            }
        }
        return temp;
    }
}
