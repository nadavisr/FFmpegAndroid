package com.example.administrator.ffmpeg;

/**
 * Created by Administrator on 28/12/2017.
 */

public interface FFmpegExecuteResponseHandler extends ResponseHandler
{
    /**
     * on Success
     * @param message complete output of the FFmpegLoader command
     */
    public void onSuccess(String message);

    /**
     * on Progress
     * @param message current output of FFmpegLoader command
     */
    public void onProgress(String message);

    /**
     * on Failure
     * @param message complete output of the FFmpegLoader command
     */
    public void onFailure(String message);
}
