package com.example.administrator.ffmpeg;

/**
 * Created by Administrator on 28/12/2017.
 */

public interface FFmpegLoadBinaryResponseHandler extends ResponseHandler {

    /**
     * on Fail
     */
    public void onFailure();

    /**
     * on Success
     */
    public void onSuccess();

}
