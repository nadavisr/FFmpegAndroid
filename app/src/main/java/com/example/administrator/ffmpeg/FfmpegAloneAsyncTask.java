package com.example.administrator.ffmpeg;

        import android.os.AsyncTask;
        import android.util.Log;

        import java.io.BufferedReader;
        import java.io.BufferedWriter;
        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.OutputStream;
        import java.io.OutputStreamWriter;
        import java.util.concurrent.TimeoutException;
        import java.io.*;
        import java.net.*;
        import java.util.*;


/**
 * Created by Administrator on 28/12/2017.
 */

public class FfmpegAloneAsyncTask extends AsyncTask<Void, String, CommandResult> {

    private final String[] cmd;
    private final FFmpegExecuteResponseHandler ffmpegExecuteResponseHandler;
    private final ShellCommand shellCommand;
    private final long timeout;
    private final ImageProvider imageProvider;
    private long startTime;
    private Process process;
    private String output = "";
    public Process getProcess(){
        return process;
    }

    private final String DRONE_IP = "172.16.10.1";
    private final int TCP_PORT = 8888;
    private final int UDP_PORT = 8895;
    private final int BUFFER_SIZE = 8192;
    private final int WIDTH = 224;
    private final int HEIGHT = 224;
    private byte[] magicBytesCtrl;
    private byte[][] magicBytesVideo1;
    private byte[] magicBytesVideo2;
    private byte[] udp_data;

    private boolean resetFlag = false;
    private boolean exitFlag = false;

    DatagramSocket udpSocket;
    InetAddress droneIpAddress;
    DatagramPacket udpPacket;

    Socket tcpSocketCtrl;
    DataOutputStream ctrlOutputStream;

    Socket tcpSocketVideo1;
    DataOutputStream video1OutputStream ;

    Socket tcpSocketVideo2;
    DataOutputStream video2OutputStream;
    DataInputStream video2InputStream;
    Timer timer;



    FfmpegAloneAsyncTask(String[] cmd, long timeout, FFmpegExecuteResponseHandler ffmpegExecuteResponseHandler, ImageProvider imageProvider) {
        this.cmd = cmd;
        this.timeout = timeout;
        this.ffmpegExecuteResponseHandler = ffmpegExecuteResponseHandler;
        this.shellCommand = new ShellCommand();
        this.imageProvider = imageProvider;
    }



    private void init()
    {

        displayVideo();

    }
    //private

    @Override
    protected void onPreExecute() {
        startTime = System.currentTimeMillis();
        if (ffmpegExecuteResponseHandler != null) {
            ffmpegExecuteResponseHandler.onStart();
        }
    }

    @Override
    protected CommandResult doInBackground(Void... params)
    {
        try {
            process = shellCommand.run(cmd);
            if (process == null)
                return CommandResult.getDummyFailureResponse();

            init();

            Log.i("Drone","doInBackground" );
            checkAndUpdateProcess();
            return CommandResult.getOutputFromProcess(process);
        }
        catch (TimeoutException e)
        {
            Log.e("FFmpegLoader timed out", e.getMessage());
            return new CommandResult(false, e.getMessage());
        }
        catch (Exception e)
        {
            Log.e("Error running FFmpegLoader", e.getMessage());
        }
        finally
        {
            Util.destroyProcess(process);
        }
        return CommandResult.getDummyFailureResponse();
    }


    private void displayVideo() {
        new Thread(new Runnable() {

            @Override
            public void run() {

                try
                {
                    Log.i("Drone","On displayVideo");
                    // File file = new File("/storage/emulated/0/Download/output.raw");
                    //Thread.sleep(150);
                    InputStream stdout = process.getInputStream(); // <- Eh?
                    int imgSize = WIDTH*HEIGHT*3;
                    byte[] buffer = new byte[imgSize];

                    //OutputStream  outputStream = new FileOutputStream`(file);
                    while(!exitFlag) {
                        try {
                            int bytesRead = 0;
                            int totalRead = 0;
                            while ((bytesRead = stdout.read(buffer, totalRead, imgSize-totalRead)) != -1) {
                                totalRead += bytesRead;

                                Log.i("Drone","displayVideo bytesRead="+bytesRead);
                                if (totalRead == imgSize) {
                                    Log.i("Drone","frame ready");
                                    imageProvider.frameReady(buffer, WIDTH, HEIGHT);

                                    break;
                                }
                                //outputStream.write(b, 0, bytesRead);
                            }
                        }
                        catch (Exception e)
                        {
                            Log.i("Drone",e.toString());
                        }
                    }

                    stdout.close();
                    //outputStream.close();
                }
                catch (Exception e)
                {
                    Log.i("Drone",e.toString());
                }
            }
        }).start();
    }


    @Override
    protected void onProgressUpdate(String... values) {
        if (values != null && values[0] != null && ffmpegExecuteResponseHandler != null) {
            ffmpegExecuteResponseHandler.onProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(CommandResult commandResult) {
        if (ffmpegExecuteResponseHandler != null) {
            output += commandResult.output;
            if (commandResult.success) {
                ffmpegExecuteResponseHandler.onSuccess(output);
            } else {
                ffmpegExecuteResponseHandler.onFailure(output);
            }
            ffmpegExecuteResponseHandler.onFinish();
        }
    }

    private void checkAndUpdateProcess() throws TimeoutException, InterruptedException
    {
        while (!Util.isProcessCompleted(process)) {
            // checking if process is completed
            if (Util.isProcessCompleted(process)) {
                return;
            }

            // Handling timeout
            if (timeout != Long.MAX_VALUE && System.currentTimeMillis() > startTime + timeout) {
                throw new TimeoutException("FFmpegLoader timed out");
            }

            try {
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    if (isCancelled()) {
                        return;
                    }

                    output += line+"\n";
                    publishProgress(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    boolean isProcessCompleted() {
        return Util.isProcessCompleted(process);
    }

}
