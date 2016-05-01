package com.example.zu.myapp.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.zu.myapp.R;
import com.example.zu.myapp.layout.LrcView;

import java.util.TreeMap;

/**
 * Created by zu on 2016/4/12.
 */
public class LrcFragment extends Fragment
{
    LrcView lrcView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout=inflater.inflate(R.layout.lyric_fragment,container,false);
        lrcView=(LrcView)layout.findViewById(R.id.lrc_view);
        return layout;
    }

    public void init()
    {
        lrcView.init();
    }

    public void setData(TreeMap<Integer,String> data)
    {
        lrcView.setData(data);

    }

    public void setTime(int time)
    {
        lrcView.setTime(time);

    }

    public void setOnLocationChangeListener(LrcView.OnLocationChangeListener listener)
    {
        lrcView.setOnLocationChangeListener(listener);
    }

    public void removeOnLocationChangerListener(LrcView.OnLocationChangeListener listener)
    {
        lrcView.removeOnLocationChangeListener(listener);
    }


}
