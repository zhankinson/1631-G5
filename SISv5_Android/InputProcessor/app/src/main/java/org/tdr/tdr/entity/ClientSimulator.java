package org.tdr.tdr.entity;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Created by mzhang on 12/2/16.
 */

public class ClientSimulator {
    private static final String TAG = "ClientSimulator";
    private static String serverAddress;
    private static int serverPort;
    private static MsgEncoder msgEncoder;

    public ClientSimulator() {
    }

    public ClientSimulator(String addr, int port) {
        serverAddress = addr;
        serverPort = port;
        Log.d(TAG, "Server Address: " + serverAddress);
        Log.d(TAG, "Server Port: " + serverPort);
    }

    public void initialize() {
        try {
            Socket socket = new Socket(serverAddress, serverPort);
            msgEncoder = new MsgEncoder(socket.getOutputStream());
        } catch (UnknownHostException e) {
            Log.d(TAG, "UnknownHostException: " + e.toString());
        } catch (IOException e) {
            Log.d(TAG, "IOException: " + e.toString());
        }
    }

    public void oneMessage() {
        try {
            KeyValueList message = random_data();
            msgEncoder.sendMsg(message);
        } catch (IOException e) {
            Log.d(TAG, "IOException: " + e.toString());
        }
    }
    public void oneMessage(String data) {
        try {
            KeyValueList message = new KeyValueList();
            message.putPair("Scope", "SIS.Scope1");
            message.putPair("MessageType", "Reading");
            message.putPair("Sender", "AndroidSensor");

            message.putPair("Data_BP", "unavailable");

            if(data.contains("EMG:") && data.contains("ECG:")){
                int index = data.indexOf("EMG:");
                int ecgindex = data.indexOf("ECG:");
                int vindex = data.indexOf("V");
                System.out.println("index:"+index);
                System.out.println("ecgindex:"+ecgindex);
                String emg = data.substring(index+4, ecgindex);
                String ecg = data.substring(ecgindex+4, vindex);
                System.out.println("emg:"+emg);
                System.out.println("ecg:"+ecg);


                message.putPair("Data_EMG", emg);
                message.putPair("Data_ECG", ecg);
            }

            message.putPair("Data_Pulse", "unavailable");

            long curr_time = System.currentTimeMillis();
            message.putPair("Data_Date", String.valueOf(curr_time));


            //KeyValueList message = random_data();
            msgEncoder.sendMsg(message);
        } catch (IOException e) {
            Log.d(TAG, "IOException: " + e.toString());
        }
    }

    public KeyValueList random_data() {
        String data;

        KeyValueList sensor_data = new KeyValueList();
        sensor_data.putPair("Scope", "SIS.Scope1");
        sensor_data.putPair("MessageType", "Reading");
        sensor_data.putPair("Sender", "AndroidSensor");

        Random r = new Random();

        data = String.valueOf(r.nextInt(10) + 75) + "/" + String.valueOf(r.nextInt(10) + 115);
        sensor_data.putPair("Data_BP", data);

        data = String.valueOf(r.nextInt(10) + 840);
        sensor_data.putPair("Data_EMG", data);

        data = "4.1" + String.valueOf(r.nextInt(10));
        sensor_data.putPair("Data_ECG", data);

        data = String.valueOf(r.nextInt(50) + 70);
        sensor_data.putPair("Data_Pulse", data);

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
        Date date = new Date();
        data = dateFormat.format(date);
        sensor_data.putPair("Data_Date", data);

        return sensor_data;
    }
}