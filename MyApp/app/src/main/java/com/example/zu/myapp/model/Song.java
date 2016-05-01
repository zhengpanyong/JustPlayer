package com.example.zu.myapp.model;

import android.graphics.Bitmap;
import android.media.Image;

/**
 * Created by zu on 2016/1/25.
 */
public class Song
{
    private long id;
    private String songName;
    private String artist;
    private String album;
    private int songSize;
    private int songDuration;
    private String buildTime;

    private String songPath;
    private String fileName;
    private long albumId;

    private Bitmap image;

    public long getId() {
        return id;
    }

    public synchronized void setId(long id) {
        this.id = id;
    }

    public long getAlbumId() {
        return albumId;
    }

    public synchronized void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public Bitmap getImage() {
        return image;
    }

    public synchronized void setImage(Bitmap image) {
        this.image = image;
    }

    public String getFileName() {
        return fileName;
    }

    public synchronized void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSongName() {
        return songName;
    }

    public synchronized void setSongName(String songName) {
        this.songName = songName;
    }

    public String getArtist() {
        return artist;
    }

    public synchronized void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public synchronized void setAlbum(String album) {
        this.album = album;
    }

    public long getSongSize() {
        return songSize;
    }

    public synchronized void setSongSize(int songSize) {
        this.songSize = songSize;
    }

    public int getSongDuration() {
        return songDuration;
    }

    public synchronized void setSongDuration(int songDuration) {
        this.songDuration = songDuration;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public synchronized void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }



    public String getSongPath() {
        return songPath;
    }

    public synchronized void setSongPath(String songPath) {
        this.songPath = songPath;
    }



    public Song()
    {

    }


}
