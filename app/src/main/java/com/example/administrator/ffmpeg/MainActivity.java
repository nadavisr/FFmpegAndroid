package com.example.administrator.ffmpeg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    TextView t;
    FFmpegLoader m_ffmpeg;
    ImageView view;
    private ImageClassifier imageClassifier;
    int i ;

    int count = 0;

    private TextView m_textView;
    SurfaceView surfaceView;
    TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.
        textureView = findViewById(R.id.textureView);
        textureView.*/

        hideSystemUI();
        m_textView = findViewById(R.id.textView);
        try {
            imageClassifier = new ImageClassifier(this);
        } catch (IOException e) {

        }


/*
        if (loadFFmpeg())
            return;
*/


        //t = findViewById(R.id.imageView);
        view = findViewById(R.id.imageView);


        ImageProvider imageProvider = new ImageProvider() {
            @Override
            public void frameReady(final byte[] rgb, final int width, final int hight) {
                try {
                    Log.i("Drone", "--------------------MainActivity received image");

                    count++;
                    if (count % 2 != 0) {
                        return;
                    }
                    String str = null;
                    if (count % 6 == 0) {
                        int num = 0;
                        String answer = imageClassifier.classifyFrame(rgb);
                        String[] arr = answer.split("\n");
                        if (arr!= null && arr.length>0)
                        {
                            String[] split = arr[1].split(":");
                            if (split != null && split.length>1)
                            {
                                double tmp = Double.valueOf(split[1]);
                                num = (int) (tmp * 100);
                                str = split[0] + " " + num + "%";
                            }
                        }
                        if (num > 50) {
                            final String finalStr = str;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    m_textView.setText(finalStr);
                                    m_textView.bringToFront();
                                }
                            });
                        }
                        else
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    m_textView.setText("");
                                    m_textView.bringToFront();
                                }
                            });
                        }

                    }

                    byte[] newArr = new byte[width * hight * 4];

                    for (int i = 0; i < hight; i++) {
                        for (int j = 0; j < width; j++) {
                            int offset = (i * hight + j) * 3;
                            int newOffset = (i * hight + j) * 4;
                            for (int k = 0; k < 4; k++) {
                                newArr[newOffset] = rgb[offset + 2];
                                newArr[newOffset + 1] = rgb[offset + 1];
                                newArr[newOffset + 2] = rgb[offset];
                                newArr[newOffset + 3] = (byte) 255;
                            }

                        }
                    }

                    final Bitmap bmp = Bitmap.createBitmap(width, hight, Bitmap.Config.ARGB_8888);
                    bmp.copyPixelsFromBuffer(ByteBuffer.wrap(newArr));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            view.setImageBitmap(bmp);
                            view.invalidate();
                        }
                    });
                } catch (Exception e) {
                    Log.i("Drone", e.toString());
                }
            }
        };

        FFmpegLoader m_ffmpeg = FFmpegLoader.getInstance(this);
        m_ffmpeg.setImageProvider(imageProvider);
        String version = "-version";
        //String[] cmd = new String[]{ "-i", "-", "-f","image2pipe", "-vcodec", "rawvideo", "-pix_fmt", "rgb24", "-"};

        ///storage/emulated/0/Download/Output.mp4
        //String[] cmd =new String[]{"-i","/storage/emulated/0/Download/bug.mov","/storage/emulated/0/Download/Output.mp4"};
        //String[] cmd =new String[]{"-i","/storage/emulated/0/Download/bug.mov", "-f", "image2pipe", "-pix_fmt", "rgb24", "-vcodec", "rawvideo", "-s", "224x224", "-"};
        String[] cmd =new String[]{"-i", "-", "-f", "image2pipe", "-pix_fmt", "bgr24", "-vcodec", "rawvideo", "-s", "224x224", "-"};
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            m_ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler()
            {

                @Override
                public void onStart() {
                    System.out.println("-------------------------ffmpeg started-----------------");
                }

                @Override
                public void onProgress(String message){
                    System.out.println("-------------------------ffmpeg on progress-----------------");
                    System.out.println(message);
                }

                @Override
                public void onFailure(String message){
                    System.out.println("-------------------------ffmpeg on failure-----------------");
                    System.out.println(message);
                //    t.setText(message);
                }

                @Override
                public void onSuccess(String message){
                    System.out.println("-------------------------ffmpeg on Success-----------------");
                    System.out.println(message);
                //    t.setText(message);
                }

                @Override
                public void onFinish() {
                    System.out.println("-------------------------ffmpeg on finish-----------------");
                }
            });
        }
        catch (FFmpegCommandAlreadyRunningException e)
        {
            // Handle if FFmpegLoader is already running
            System.out.println("Handle if FFmpegLoader is already running");
        }


    }

    private boolean loadFFmpeg()
    {
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onFailure() {}

                @Override
                public void onSuccess() {
                    Log.d("--------installed----","Success");
                }

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpegLoader is not supported by device
        }
        return true;
    }

    private void hideSystemUI() {
        View mDecorView = getWindow().getDecorView();

        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
