/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robomus.instrument.fretted.lapsteelguitar;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


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
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Higor
 */
public class Buffer extends RobotAction{

    private List<OSCMessage> messages;
    private List<Integer> idProcessedMsg;
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
    private OutputStreamWriter fOutLog;


    public Buffer(Activity act, MyRobot myRobot, OutputStreamWriter fOut,
                  OutputStreamWriter fOutLog) {
        super(myRobot);

        this.messages = new ArrayList<OSCMessage>();
        this.idProcessedMsg = new ArrayList<Integer>();
        this.usbService = usbService;
        Log.d("buffer","criouu");
        this.activity = act;
        View v = activity.findViewById(R.id.view);
        v.setBackgroundColor(0xFFcc0000);


        this.fOut = fOut;
        this.fOutLog = fOutLog;
        try {
            this.fOut.append("timeSleep timeExe id\n");
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public InetAddress getServerIp() {
        return serverIp;
    }

    public void setServerIp(InetAddress serverIp) {
        this.serverIp = serverIp;
    }

    public String getServerOscAddress() {
        return serverOscAddress;
    }

    public void setServerOscAddress(String serverOscAddress) {
        this.serverOscAddress = serverOscAddress;
    }

    /*
      * Method to get the original message id from the server. The msg from server is converted to a unique byte.
      * Example: server msg id 7500 -> 7500%256 = 76 (id to send to arduino)
      * @paran format represents the msg format
     */
    public int getIdConfirmMessage(int idFromArduino){
        imprimirId();

        for (Integer id: this.idProcessedMsg) {
            Log.i("comparador", Integer.toString(id%128) +" - ardId= "+ idFromArduino );
            if( (id%128) == idFromArduino ){
                this.idProcessedMsg.remove(id);
                return id;
            }
        }
        return -1;
    }
    public void imprimirId(){
        for (Integer id: this.idProcessedMsg) {
            Log.i("vetorId = ",id.toString());
        }
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

    /*
     * pega o cabeçalho da msg
     * @paran msg osc
     */
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

    /*
      * Method to write a message OSC on screep smartphone to debug
      * @paran format represents the msg format
      * @paran oscMessage the message to write on the screen
     */
    public void writeMsgLog(final String format , final OSCMessage oscMessage){

        final TextView txtLog = (TextView) this.activity.findViewById(R.id.textViewLog);

        String txt = "";
        for(Object arg: oscMessage.getArguments()){
            txt += arg.toString()+",";

        }

        final StringBuffer sb = new StringBuffer(txt);
        sb.setCharAt(txt.length()-1, ']');
        //saving in a file log
        try {
            this.fOutLog.append("\n"+format+"\n Adress: "+oscMessage.getAddress()+" ["+sb.toString()+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.activity.runOnUiThread(new Runnable()
        {

            @Override
            public void run() {
                if(txtLog.getText().length() > 2000){
                    txtLog.setText("\n"+format+"\n");
                    txtLog.setText("Adress: "+oscMessage.getAddress()+" ["+sb.toString()+"\n");
                }else{
                    txtLog.append("\n"+format+"\n");
                    txtLog.append("Adress: "+oscMessage.getAddress()+" ["+sb.toString()+"\n");
                }

            }
        });


    }



    public void run() {
        
        Long timeSleep = (long)0;
        String header;
        final TextView txtLog = (TextView) this.activity.findViewById(R.id.textViewLog);

        while(true){

            if (!this.messages.isEmpty()) {

                final OSCMessage oscMessage = messages.get(0);
                //add the msg id in a list
                this.idProcessedMsg.add((int) oscMessage.getArguments().get(1));

                Log.i("msg",messages.get(0).getArguments().get(0).toString());
                timeSleep= Long.parseLong(messages.get(0).getArguments().get(0).toString());

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
                                this.writeMsgLog("Blink: Format = [timeSleep, collor]",oscMessage);
                                break;
                            case "synchronize":
                                break;
                            case "playNote":
                                this.playNote(oscMessage);
                                this.writeMsgLog("playNote: Format = [timeSleep, id, note]",oscMessage);
                                break;
                            case "playNoteFretted":
                                break;
                            case "playString":
                                this.playString(oscMessage);
                                this.writeMsgLog("playString: Format = [timeSleep, id, string]",oscMessage);
                                break;
                            case "slide":
                                this.slide(oscMessage);
                                this.writeMsgLog("slide: Format = [timestamp, id, start position, end position]",oscMessage);
                                break;
                            case "moveBar":
                                this.moveBar(oscMessage);
                                this.writeMsgLog("moveBar: Format = [timestamp, id, position] (0->down, 1->up)",oscMessage);
                                break;
                            case "positionBar":
                                this.positionBar(oscMessage);
                                this.writeMsgLog("positionBar: Format OSC = [timestamp, id, fretPosition]",oscMessage);
                                break;
                            case "volumeControl":
                                break;
                            case "toneControl":
                                break;
                            case "fuzz":
                                break;
                            case "stop":
                                break;
                            case "playNoteTest":
                                this.playNoteTest(oscMessage);
                                this.writeMsgLog("playNoteTest: Format OSC = [fret, string]",oscMessage);
                                break;
                            case "testeMsg":
                                this.testMsg(oscMessage);
                                this.writeMsgLog("testeMsg: Format OSC = [timestamp, id]",oscMessage);
                                break;
                            case "handshake":
                                //this.testMsg(oscMessage);
                                this.writeMsgLog("handshake: Format OSC = [oscAdd, ip, port]",oscMessage);
                                break;
                            case "playSound":
                                this.playSound(oscMessage);
                                this.writeMsgLog("testeMsg: Format OSC = [timestamp, id]",oscMessage);
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
