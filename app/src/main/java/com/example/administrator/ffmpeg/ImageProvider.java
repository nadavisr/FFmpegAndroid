package com.example.administrator.ffmpeg;

/**
 * Created by Administrator on 01/01/2018.
 */

public interface ImageProvider {
    void frameReady(byte[] rgb, int width, int hight);
}
