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

public class FFmpegExecuteAsyncTask extends AsyncTask<Void, String, CommandResult>  {

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

    private final int[] magicBytesCtrl_int = new int []{
            0x49, 0x54, 0x64, 0x00, 0x00, 0x00, 0x5D, 0x00, 0x00, 0x00, 0x81, 0x85, 0xFF, 0xBD, 0x2A, 0x29, 0x5C, 0xAD, 0x67, 0x82, 0x5C, 0x57, 0xBE, 0x41, 0x03, 0xF8, 0xCA, 0xE2, 0x64, 0x30, 0xA3, 0xC1,
            0x5E, 0x40, 0xDE, 0x30, 0xF6, 0xD6, 0x95, 0xE0, 0x30, 0xB7, 0xC2, 0xE5, 0xB7, 0xD6, 0x5D, 0xA8, 0x65, 0x9E, 0xB2, 0xE2, 0xD5, 0xE0, 0xC2, 0xCB, 0x6C, 0x59, 0xCD, 0xCB, 0x66, 0x1E, 0x7E, 0x1E,
            0xB0, 0xCE, 0x8E, 0xE8, 0xDF, 0x32, 0x45, 0x6F, 0xA8, 0x42, 0xEE, 0x2E, 0x09, 0xA3, 0x9B, 0xDD, 0x05, 0xC8, 0x30, 0xA2, 0x81, 0xC8, 0x2A, 0x9E, 0xDA, 0x7F, 0xD5, 0x86, 0x0E, 0xAF, 0xAB, 0xFE,
            0xFA, 0x3C, 0x7E, 0x54, 0x4F, 0xF2, 0x8A, 0xD2, 0x93, 0xCD
    };

    private final int[][] magicBytesVideo1_int =  new int[][]
            {
            {0x49, 0x54, 0x64, 0x00, 0x00, 0x00, 0x52, 0x00, 0x00, 0x00, 0x0F, 0x32, 0x81, 0x95, 0x45, 0x2E, 0xF5, 0xE1, 0xA9, 0x28, 0x10, 0x86, 0x63, 0x17, 0x36, 0xC3, 0xCA, 0xE2, 0x64, 0x30, 0xA3, 0xC1,
                0x5E, 0x40, 0xDE, 0x30, 0xF6, 0xD6, 0x95, 0xE0, 0x30, 0xB7, 0xC2, 0xE5, 0xB7, 0xD6, 0x5D, 0xA8, 0x65, 0x9E, 0xB2, 0xE2, 0xD5, 0xE0, 0xC2, 0xCB, 0x6C, 0x59, 0xCD, 0xCB, 0x66, 0x1E, 0x7E, 0x1E,
                0xB0, 0xCE, 0x8E, 0xE8, 0xDF, 0x32, 0x45, 0x6F, 0xA8, 0x42, 0xB7, 0x33, 0x0F, 0xB7, 0xC9, 0x57, 0x82, 0xFC, 0x3D, 0x67, 0xE7, 0xC3, 0xA6, 0x67, 0x28, 0xDA, 0xD8, 0xB5, 0x98, 0x48, 0xC7, 0x67,
                0x0C, 0x94, 0xB2, 0x9B, 0x54, 0xD2, 0x37, 0x9E, 0x2E, 0x7A},
            {
            0x49, 0x54, 0x64, 0x00, 0x00, 0x00, 0x52, 0x00, 0x00, 0x00, 0x54, 0xB2, 0xD1, 0xF6, 0x63, 0x48, 0xC7, 0xCD, 0xB6, 0xE0, 0x5B, 0x0D, 0x1D, 0xBC, 0xA8, 0x1B, 0xCA, 0xE2, 0x64, 0x30, 0xA3, 0xC1,
            0x5E, 0x40, 0xDE, 0x30, 0xF6, 0xD6, 0x95, 0xE0, 0x30, 0xB7, 0xC2, 0xE5, 0xB7, 0xD6, 0x5D, 0xA8, 0x65, 0x9E, 0xB2, 0xE2, 0xD5, 0xE0, 0xC2, 0xCB, 0x6C, 0x59, 0xCD, 0xCB, 0x66, 0x1E, 0x7E, 0x1E,
            0xB0, 0xCE, 0x8E, 0xE8, 0xDF, 0x32, 0x45, 0x6F, 0xA8, 0x42, 0xB7, 0x33, 0x0F, 0xB7, 0xC9, 0x57, 0x82, 0xFC, 0x3D, 0x67, 0xE7, 0xC3, 0xA6, 0x67, 0x28, 0xDA, 0xD8, 0xB5, 0x98, 0x48, 0xC7, 0x67,
            0x0C, 0x94, 0xB2, 0x9B, 0x54, 0xD2, 0x37, 0x9E, 0x2E, 0x7A
            }};

    private final int[] magicBytesVideo2_int = new int[]{
                                     0x49, 0x54, 0x64, 0x00, 0x00, 0x00, 0x58, 0x00, 0x00, 0x00, 0x80, 0x86, 0x38, 0xC3, 0x8D, 0x13, 0x50, 0xFD, 0x67, 0x41, 0xC2, 0xEE, 0x36, 0x89, 0xA0, 0x54, 0xCA, 0xE2, 0x64, 0x30, 0xA3, 0xC1,
                                     0x5E, 0x40, 0xDE, 0x30, 0xF6, 0xD6, 0x95, 0xE0, 0x30, 0xB7, 0xC2, 0xE5, 0xB7, 0xD6, 0x5D, 0xA8, 0x65, 0x9E, 0xB2, 0xE2, 0xD5, 0xE0, 0xC2, 0xCB, 0x6C, 0x59, 0xCD, 0xCB, 0x66, 0x1E, 0x7E, 0x1E,
                                     0xB0, 0xCE, 0x8E, 0xE8, 0xDF, 0x32, 0x45, 0x6F, 0xA8, 0x42, 0xEB, 0x20, 0xBE, 0x38, 0x3A, 0xAB, 0x05, 0xA8, 0xC2, 0xA7, 0x1F, 0x2C, 0x90, 0x6D, 0x93, 0xF7, 0x2A, 0x85, 0xE7, 0x35, 0x6E, 0xFF,
                                     0xE1, 0xB8, 0xF5, 0xAF, 0x09, 0x7F, 0x91, 0x47, 0xF8, 0x7E
        };

    private final int[] data_int = new  int[] {0xCC, 0x7F, 0x7F, 0x0, 0x7F, 0x0, 0x7F, 0x33};


    FFmpegExecuteAsyncTask(String[] cmd, long timeout, FFmpegExecuteResponseHandler ffmpegExecuteResponseHandler, ImageProvider imageProvider) {
        this.cmd = cmd;
        this.timeout = timeout;
        this.ffmpegExecuteResponseHandler = ffmpegExecuteResponseHandler;
        this.shellCommand = new ShellCommand();
        this.imageProvider = imageProvider;
    }

    private void initData()
    {
        magicBytesCtrl = new byte[magicBytesCtrl_int.length];
        convertToByteArr(magicBytesCtrl_int, magicBytesCtrl);

        magicBytesVideo2 = new byte[magicBytesVideo2_int.length];
        convertToByteArr(magicBytesVideo2_int, magicBytesVideo2);

        udp_data = new byte[data_int.length];
        convertToByteArr(data_int, udp_data);

        magicBytesVideo1 = new byte[2][];
        magicBytesVideo1[0] = new byte[magicBytesVideo1_int[0].length];
        convertToByteArr(magicBytesVideo1_int[0], magicBytesVideo1[0]);

        magicBytesVideo1[1] = new byte[magicBytesVideo1_int[1].length];
        convertToByteArr(magicBytesVideo1_int[1], magicBytesVideo1[1]);

    }

    private void convertToByteArr(int[] src, byte[] dest)
    {
        if(src.length != dest.length)
            return;

        for(int i=0; i< src.length; i++)
        {
            dest[i] = (byte)src[i];
        }
    }

    private void init()
    {
        Log.i("Drone","on init" );
        initData();
        receiveFromDrone();
        displayVideo();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                resetFlag = true;
            }
        }, 10*1000, 10*1000);
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

    private void initComm() throws java.io.IOException {
        try {
            udpSocket = new DatagramSocket(UDP_PORT);
            droneIpAddress = InetAddress.getByName(DRONE_IP);
            udpPacket = new DatagramPacket(udp_data, udp_data.length, droneIpAddress, UDP_PORT);

            tcpSocketCtrl = new Socket(DRONE_IP, TCP_PORT);
            ctrlOutputStream = new DataOutputStream(tcpSocketCtrl.getOutputStream());

            tcpSocketVideo1 = new Socket(DRONE_IP, TCP_PORT);
            tcpSocketVideo1.setSoTimeout(500);
            video1OutputStream = new DataOutputStream(tcpSocketVideo1.getOutputStream());

            tcpSocketVideo2 = new Socket(DRONE_IP, TCP_PORT);
            tcpSocketVideo2.setSoTimeout(500);
            video2OutputStream = new DataOutputStream(tcpSocketVideo2.getOutputStream());
            video2InputStream = new DataInputStream(tcpSocketVideo2.getInputStream());
        }catch (Exception e)
        {
            Log.i("Drone","e" );
        }


//        outToServer.writeBytes(sentence + '\n');
//        modifiedSentence = inFromServer.readLine();
//        System.out.println("FROM SERVER: " + modifiedSentence);
//        clientSocket.close();
    }

    private void sendMagicPacket1() throws java.io.IOException, java.lang.InterruptedException
    {
        video1OutputStream.write(magicBytesVideo1[0]);
        Thread.sleep(10);
        video1OutputStream.write(magicBytesVideo1[1]);
        Thread.sleep(10);
        Log.i("Drone","sendMagicPacket1" );
    }

    private void sendMagicPacket2() throws java.io.IOException, java.lang.InterruptedException
    {
        video2OutputStream.write(magicBytesVideo2);
        Log.i("Drone","sendMagicPacket2" );
    }

    private void sendUdp() throws java.io.IOException, java.lang.InterruptedException
    {
        udpSocket.send(udpPacket);
        Log.i("Drone","sendUdp Data" );
    }

    private void sendAllPackets() throws java.io.IOException, java.lang.InterruptedException
    {
        sendUdp();
        sendMagicPacket1();
        sendMagicPacket2();
        Log.i("Drone","sendAllPackets" );
    }

    private void closeComm() throws java.io.IOException, java.lang.InterruptedException
    {
        if(video1OutputStream != null)
            video1OutputStream.close();

        if(video2OutputStream != null)
            video2OutputStream.close();

        if(ctrlOutputStream != null)
            ctrlOutputStream.close();

        if(udpSocket != null)
            udpSocket.close();

        Log.i("Drone","closeComm" );

    }

    private void resetComm() throws java.io.IOException, java.lang.InterruptedException
    {
        closeComm();
        Thread.sleep(100);
        initComm();
        Thread.sleep(100);
        sendAllPackets();
        resetFlag = false;

        Log.i("Drone","resetComm" );
    }

    private void receiveFromDrone() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                OutputStream stdin = null;
                Log.i("Drone","On receiveFromDrone");
                //File file = new File("/storage/emulated/0/Download/bug.mov");

                    try {
                        //InputStream inputStream = new FileInputStream(file);

                        stdin = process.getOutputStream();
                        byte[] buffer = new byte[65000];
                        int bytesRead = 0;

                         while(!exitFlag) {
                             resetComm();

                             while (!resetFlag) {
                                 try {
                                     bytesRead = video2InputStream.read(buffer, 0, 8192);
                                     //Log.i("Drone","inside while");
                                     //bytesRead = inputStream.read(buffer, 0, 8192);
                                     if (bytesRead > 0) {
                                         if(bytesRead == 40)
                                             continue;

                                         stdin.write(buffer, 0, bytesRead);
                                         Log.i("Drone","receiveFromDrone write " + bytesRead + "to stdin");
                                     } else
                                         Thread.sleep(10);
                                 } catch (Exception e) {
                                     Log.i("Drone",e.toString());
                                 }
                             }
                         }

                        stdin.close();
                    }
                    catch (Exception e) {
                        Log.i("Drone",e.toString());
                    }

            }
        }).start();
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

                    //OutputStream  outputStream = new FileOutputStream(file);
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
