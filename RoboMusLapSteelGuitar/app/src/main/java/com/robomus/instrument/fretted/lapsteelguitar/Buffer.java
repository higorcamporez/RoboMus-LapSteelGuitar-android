/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robomus.instrument.fretted.lapsteelguitar;

import android.app.Activity;
import android.util.Log;
import android.view.View;


import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;
import com.robomus.arduinoCommunication.UsbService;
import com.robumus.higor.robomuslapsteelguitar.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Higor
 */
public class Buffer extends RobotAction{
    
    private List<OSCMessage> messages;
    private long lastServerTime;
    private long lastInstrumentTime;
    private int thresold = 100;
    private Activity activity;
    private boolean viewColor = false;
    private InetAddress serverIp;
    private String serverOscAddress;
    private int serverPort;
    private UsbService usbService;
    private OutputStreamWriter fOut;


    public Buffer(Activity act, InetAddress serverIp, String serverOscAddress, int serverPort,
                  UsbService usbService, OutputStreamWriter fOut) {
        super(usbService);
        this.messages = new ArrayList<OSCMessage>();
        this.usbService = usbService;
        Log.d("buffer","criouu");
        this.activity =act;
        View v = activity.findViewById(R.id.view);
        v.setBackgroundColor(0xFFcc0000);
        this.serverIp = serverIp;
        this.serverOscAddress = serverOscAddress;
        this.serverPort= serverPort;
        this.fOut = fOut;
        try {
            this.fOut.append("timeSleep timeExe id\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        String aux = new String("apenas um teste, pode cre?");
        try {
            fOut.write(aux);
            fOut.close();
            Log.i("arq", "gravoi");
        } catch (IOException e) {
            e.printStackTrace();
        }*/





    }
    
    public OSCMessage remove(){
        return messages.remove(0);
    }
    public void add(OSCMessage l){
        //comentario
        if(l.getArguments().get(0).equals("/synch") ){
            messages.add(0,l);
        }else{

        }
        messages.add(messages.size(), l);
    }
    public void remove(int n){
        for (int i = 0; i < n; i++) {
            messages.remove(i);
        }
    }
    
    public OSCMessage get(){
        if(messages.isEmpty()){
            return messages.get(0);
        }
        return null;
    }
    public long relativeTime(){
        return (this.lastServerTime + Math.abs( System.currentTimeMillis() - this.lastInstrumentTime) );
    }
    
    public Long getFirstTimestamp(){
        
        OSCMessage oscMsg = messages.get(0);
        System.out.println(oscMsg.getArguments().size());
        return (Long)oscMsg.getArguments().get(0);
        
    }
    
    public void print(){
        int cont =0;
        System.out.println("_________________buffer______________");
        
        for (OSCMessage message : messages) {
            System.out.println("------------ posicao = "+cont+" -------------");
            for (Object obj : message.getArguments()) {
               
                System.out.println(obj);
            }
            cont++;
        }
        System.out.println("_____________ End buffer_______________");
    }
    
    public void synch(OSCMessage oscMessage){

        List<Object> args = oscMessage.getArguments();

            this.lastServerTime = (Long)args.get(0);
            this.lastInstrumentTime = System.currentTimeMillis();


        remove();
        
    }
    public void synchStart(OSCMessage oscMessage){
        List<Object> args = oscMessage.getArguments();
        
        this.lastServerTime = (Long)args.get(0);
        this.lastInstrumentTime = System.currentTimeMillis();
        this.thresold = (int)args.get(1);
        remove();
    }
    public void blink(OSCMessage msg) {
        final int collor = (int) msg.getArguments().get(1);
        final boolean s = this.viewColor;
        this.activity.runOnUiThread(new Runnable()
        {

            @Override
            public void run() {
                if(collor == 1){
                    activity.findViewById(R.id.view).setBackgroundColor(0xFFF2F205);
                }else if(collor == 2){
                    activity.findViewById(R.id.view).setBackgroundColor(0xFFFEB135);
                }else if(collor == 3){
                    activity.findViewById(R.id.view).setBackgroundColor(0xFF2dbfff);
                }else if(collor == 4){
                    activity.findViewById(R.id.view).setBackgroundColor(0xFF000470);
                }


            }
        });
        byte[] b = {1};
        if(this.usbService != null){
            this.usbService.write(b);
        }

        try {

            fOut.append(msg.getArguments().get(0).toString()
                    +" "+System.currentTimeMillis()+" "+msg.getArguments().get(2).toString()+"\n");

        } catch (IOException e) {
            e.printStackTrace();
        }

        //send msg confirm msg
        /*
        OSCPortOut sender = null;
        try {

                sender = new OSCPortOut(this.serverIp , this.serverPort);

                OSCMessage oscMessage = new OSCMessage(this.serverOscAddress +"/blink/"+"laplap");
                //oscMessage.addArgument(time);
                oscMessage.addArgument(msg.getArguments().get(2));


                try {
                    sender.send(oscMessage);
                    Log.e("resp","ip="+this.serverIp+"port="+this.serverPort+"oscAdr="+this.serverOscAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                }


        } catch (SocketException e) {
            e.printStackTrace();
        }*/

        //write in a txt file for debug


    }

    public String getHeader(OSCMessage oscMessage){
        String header = (String) oscMessage.getAddress();
                    
        if(header.startsWith("/"))
            header = header.substring(1);

        String[] split = header.split("/", -1);

        if (split.length >= 2) {
            header = split[1];
        }else{
            header = null;
        }
        return header;
    }
    
    public void run() {
        
        int timeSleep;
        String header;
        while(true){
            //System.out.println("nao tem condições");
            if (!this.messages.isEmpty()) {

                OSCMessage oscMessage = messages.get(0);
                Log.i("msg",messages.get(0).getArguments().get(0).toString());
                timeSleep= (int) messages.get(0).getArguments().get(0);

                header = getHeader(oscMessage);
                Log.e("buffer", "Header="+header+" timemsg="+timeSleep);

                if(header.equals("synch")){
                    Log.d("synch","synch");
                    synch(oscMessage);
                }else if(header.equals("synchStart")){
                     Log.d("synch","synchStart");
                    synchStart(oscMessage);
                }else{
                    
                    if (header != null) {
                        
                        System.out.println("Adress = " + header);

                        switch (header) {
                            case "blink":
                                Log.d("action","blink");
                                blink(oscMessage);
                                break;
                            case "synchronize":
                                break;
                            case "playNote":
                                
                                break;
                            case "playNoteFretted":
                                break;
                            case "playString":
                                this.playString(oscMessage);
                                break;
                            case "slide":
                                break;
                            case "moveBar":
                                this.moveBar(oscMessage);
                                break;
                            case "positionBar":
                                this.positionBar(oscMessage);
                                break;
                            case "volumeControl":
                                break;
                            case "toneControl":
                                break;
                            case "fuzz":
                                break;
                            case "stop":
                                break;
                            
                                
                        }
                        try {
                            Thread.sleep(timeSleep);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        remove();

                    }

                }
            }
        }
    }
    
    
}
