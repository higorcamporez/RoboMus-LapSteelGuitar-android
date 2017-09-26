/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robomus.instrument.fretted.lapsteelguitar;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.robomus.arduinoCommunication.UsbService;
import com.robomus.instrument.fretted.FrettedInstrument;
import com.robomus.instrument.fretted.InstrumentString;
import com.robomus.util.Note;
import com.robumus.higor.robomuslapsteelguitar.R;


/**
 *
 * @author Higor ghghghghg
 */
public class MyRobot extends FrettedInstrument{
    
    private volatile Buffer buffer;
    private OSCPortOut sender;
    private OSCPortIn receiver;
    private Activity activity;
    private OutputStreamWriter fOut;
    private OutputStreamWriter fOutLog;
    private UsbService usbService;
    private Boolean emulate;
    private ToneBar toneBar;

    //private PortControl portControl;
    
    public MyRobot( String name, String OscAddress, int receivePort,
                    UsbService usbService, Activity act,
                    OutputStreamWriter fOut, OutputStreamWriter fOutLog, String myIp) {

        super( name, OscAddress, receivePort, myIp);

        /* lapla informations*/

        ArrayList<InstrumentString> instrStrings = new ArrayList<InstrumentString>();

        instrStrings.add(new InstrumentString(1, new Note("G4")));
        instrStrings.add(new InstrumentString(2, new Note("F4")));
        instrStrings.add(new InstrumentString(3, new Note("E4")));
        instrStrings.add(new InstrumentString(4, new Note("C4")));
        instrStrings.add(new InstrumentString(5, new Note("A3")));
        instrStrings.add(new InstrumentString(6, new Note("F3")));
        instrStrings.add(new InstrumentString(7, new Note("D3")));
        instrStrings.add(new InstrumentString(8, new Note("A2")));
        instrStrings.add(new InstrumentString(9, new Note("D2")));

        this.instrumentStrings = instrStrings;
        this.nFrets = 12;
        this.polyphony = 9;
        this.typeFamily = "Fretted";
        this.specificProtocol = "</playSound;frequency_i;durationSeg_i>" +
                                "</playNote;note_s>"+
                                "</playHappyBirth>";
        this.emulate = true;
        /*end laplap informations*/

        TextView txtLog = (TextView) act.findViewById(R.id.textViewLog);
        this.activity = act;
        //imprimir inicio do log com data e hora
        DateFormat df = new SimpleDateFormat("dd MM yyyy, HH:mm");
        String date = df.format(Calendar.getInstance().getTime());

        txtLog.append("---- ROBOMUS LOG "+ date+ " ----\n");

        //fim imprimir log

        this.fOut = fOut;
        this.fOutLog = fOutLog;
        this.usbService = usbService;

        this.buffer = new Buffer(this.activity, this, fOut, fOutLog);
        this.toneBar = new ToneBar();

        //Initializing the OSC Receiver
        this.receiver = null;
        try {

            this.receiver = new OSCPortIn(this.receivePort);
        }
        catch (SocketException ex) {
            Logger.getLogger(MyRobot.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

    public UsbService getUsbService() {
        return usbService;
    }

    public Boolean getEmulate() {
        return emulate;
    }

    public ToneBar getToneBar() {
        return toneBar;
    }

    public void setToneBar(ToneBar toneBar) {
        this.toneBar = toneBar;
    }

    public void handshake(){

        List args = new ArrayList<>();
        
        //instrument attributes
        args.add(this.name);
        args.add(this.myOscAddress);
        args.add(this.myIp);
        args.add(this.receivePort);
        args.add(this.polyphony);
        args.add(this.typeFamily);
        args.add(this.specificProtocol);

        //amount of attributes
        args.add(2);
        //fretted instrument attributs
        args.add(this.nFrets); //number of frets
        args.add(convertInstrumentoStringToString()); // strings and turnings

      
	    OSCMessage msg = new OSCMessage("/handshake/instrument", args);
        OSCPortOut sender = null;

        try {
            //send de msg with the broadcast ip
            String s = this.myIp;
            String[] ip = s.split("\\.");
            String broadcastIp = ip[0]+"."+ip[1]+"."+ip[2]+".255";
            sender = new OSCPortOut(InetAddress.getByName(broadcastIp), this.receivePort);
        } catch (SocketException ex) {
            Logger.getLogger(MyRobot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
             
        try {
            sender.send(msg);
        } catch (IOException ex) {
            Logger.getLogger(MyRobot.class.getName()).log(Level.SEVERE, null, ex);
        }
                       
    }
    private void startBuffer(OSCMessage message){
        this.serverName = message.getArguments().get(0).toString();
        this.serverOscAddress = message.getArguments().get(1).toString();
        this.severIpAddress = message.getArguments().get(2).toString();
        this.sendPort = Integer.parseInt(message.getArguments().get(3).toString());
        //log screen
        final TextView txtLog = (TextView) this.activity.findViewById(R.id.textViewLog);
        final String s = "handshake: Format OSC = [oscAdd, ip, port]\n Adress:"+ message.getAddress()+
                " ["+ this.serverOscAddress+", "+ this.severIpAddress+", "+this.sendPort+"]\n";
        this.activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run() {
                txtLog.append(s);

            }
        });

        //end log screen
        try {
            this.buffer.setServerIp(InetAddress.getByName(this.severIpAddress) );
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.buffer.setServerOscAddress(this.serverOscAddress);
        this.buffer.setServerPort(this.sendPort);

        this.buffer.start();

        //Initializing the OSC sender
        this.sender = null;
        try {
            this.sender = new OSCPortOut(InetAddress.getByName(this.severIpAddress), this.sendPort);
        } catch (SocketException ex) {
            Logger.getLogger(MyRobot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


    }

    public void listenThread(){
        System.out.println("Inicio p="+ this.receivePort+" end="+this.myOscAddress);

        OSCListener listener = new OSCListener() {

            public void acceptMessage(java.util.Date time, OSCMessage message) {
                Log.d("buffer","recebeu msg");
                String header = (String) message.getAddress();
                header = header.substring(1);
                String[] split = header.split("\\/", -1);
                if(split.length == 2 && split[1].equals("handshake")){
                    startBuffer(message);
                }else{
                    List l = message.getArguments();
                    for (Object l1 : l) {
                        Log.d("buffer","object=" + l1);
                    }
                    //verificar se a mensagem é válida

                    buffer.add(message);
                }
            }
        };
        this.receiver.addListener(this.myOscAddress+"/*", listener);
        this.receiver.startListening();
        
    }
    public void ConfirmMsgToServ(){

        List args = new ArrayList<>();
        args.add("action completed");

        OSCMessage msg = new OSCMessage(this.serverOscAddress+"/action/"+this.name, args);
             
        try {
            this.sender.send(msg);
        } catch (IOException ex) {
            Logger.getLogger(MyRobot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
   
    public void sendConfirmActionMessage( int idFromArduino){
        int idConveted = this.buffer.getIdConfirmMessage(idFromArduino);
        Byte b;

        Log.i("teste", "idConv="+idConveted);
        if(idConveted != -1){
            List args = new ArrayList<>();
            args.add(idConveted);
            OSCMessage msg = new OSCMessage(this.serverOscAddress+"/action", args);
            System.out.println("enviou conf");

            try {
                this.sender.send(msg);
            } catch (IOException ex) {
                Logger.getLogger(MyRobot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }
    public void disconnect(){
        List args = new ArrayList<>();
        args.add(this.myOscAddress);
        OSCMessage msg = new OSCMessage(this.serverOscAddress+"/disconnect/instrument", args);

        try {
            this.sender.send(msg);
            receiver.stopListening();
            buffer.interrupt();
        } catch (IOException ex) {
            Logger.getLogger(MyRobot.class.getName()).log(Level.SEVERE, null, ex);
        }


    }



        
    
}
