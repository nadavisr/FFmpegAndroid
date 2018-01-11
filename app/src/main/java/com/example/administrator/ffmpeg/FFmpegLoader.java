package com.example.administrator.ffmpeg;

import android.content.Context;
import java.lang.reflect.Array;
import java.util.Map;

/**
 * Created by Administrator on 28/12/2017.
 */

public class FFmpegLoader implements FFmpegInterface {

    private final Context context;
    public FFmpegExecuteAsyncTask ffmpegExecuteAsyncTask;

    private long timeout = Long.MAX_VALUE;

    private static FFmpegLoader instance = null;
    private ImageProvider imageProvider;

    private FFmpegLoader(Context context) {
        this.context = context.getApplicationContext();

    }

    public static FFmpegLoader getInstance(Context context) {
        if (instance == null) {
            instance = new FFmpegLoader(context);
        }
        return instance;
    }


    @Override
    public void execute(Map<String, String> environvenmentVars, String[] cmd, FFmpegExecuteResponseHandler ffmpegExecuteResponseHandler) throws FFmpegCommandAlreadyRunningException {
        if (ffmpegExecuteAsyncTask != null && !ffmpegExecuteAsyncTask.isProcessCompleted()) {
            throw new FFmpegCommandAlreadyRunningException("FFmpegLoader command is already running, you are only allowed to run single command at a time");
        }
        if (cmd.length != 0) {
            String[] ffmpegBinary = new String[] { FileUtils.getFFmpeg(context, environvenmentVars) };
            String[] command = concatenate(ffmpegBinary, cmd);
            ffmpegExecuteAsyncTask = new FFmpegExecuteAsyncTask(command,timeout, ffmpegExecuteResponseHandler, this.imageProvider);
            ffmpegExecuteAsyncTask.execute();
        } else {
            throw new IllegalArgumentException("shell command cannot be empty");
        }
    }

    public <T> T[] concatenate (T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    @Override
    public void execute(String[] cmd,FFmpegExecuteResponseHandler ffmpegExecuteResponseHandler)throws FFmpegCommandAlreadyRunningException {
        execute(null, cmd, ffmpegExecuteResponseHandler);
    }

    public void setImageProvider(ImageProvider imageProvider) {
        this.imageProvider = imageProvider;
    }
}