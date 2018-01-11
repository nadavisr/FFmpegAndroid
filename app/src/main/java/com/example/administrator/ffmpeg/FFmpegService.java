package com.example.administrator.ffmpeg;

import android.content.Context;

/**
 * Created by Administrator on 02/01/2018.
 */

public class FFmpegService implements FFmpegServiceInterface {

    private Context context;
    private ImageProvider imageProvider;

    public FFmpegService(ImageProvider image, Context context)
    {
        this.imageProvider = image;
        this.context = context;
    }
    @Override
    public void start() {
        FFmpegLoader m_ffmpeg = FFmpegLoader.getInstance(context);
        m_ffmpeg.setImageProvider(imageProvider);
        String version = "-version";
        //String[] cmd = new String[]{ "-i", "-", "-f","image2pipe", "-vcodec", "rawvideo", "-pix_fmt", "rgb24", "-"};

        ///storage/emulated/0/Download/Output.mp4
        // String[] cmd =new String[]{"-i","/storage/emulated/0/Download/bug.mov","/storage/emulated/0/Download/Output.mp4"};
        String[] cmd =new String[]{"-i", "-", "-f", "image2pipe", "-pix_fmt", "bgr32", "-vcodec", "rawvideo", "-"};
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            m_ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler()
            {

                @Override
                public void onStart() { }

                @Override
                public void onProgress(String message){
                    //t.setText(message);
                }

                @Override
                public void onFailure(String message){
                    //    t.setText(message);
                }

                @Override
                public void onSuccess(String message){
                    //    t.setText(message);
                }

                @Override
                public void onFinish() { }
            });
        }
        catch (FFmpegCommandAlreadyRunningException e)
        {
            // Handle if FFmpegLoader is already running
            System.out.println("Handle if FFmpegLoader is already running");
        }
    }

    @Override
    public void stop() {

    }
}
