package com.example.zu.myapp.fragment;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.zu.myapp.R;

/**
 * Created by zu on 2016/4/11.
 */
public class ArtworkFragment extends Fragment
{
    ImageView songPic;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout=inflater.inflate(R.layout.art_fragment,container,false);
        songPic=(ImageView)layout.findViewById(R.id.art_work_image_view);
        return layout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void setSongPic(Bitmap pic)
    {
        songPic.setImageBitmap(pic);
    }
}
