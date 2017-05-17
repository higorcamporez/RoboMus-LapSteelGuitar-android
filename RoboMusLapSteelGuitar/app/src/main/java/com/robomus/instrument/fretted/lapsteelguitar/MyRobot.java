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

    //private PortControl portControl;
    
    public MyRobot(int nFrets, ArrayList<InstrumentString> strings, String name,
                   int polyphony, String OscAddress, int receivePort, String typeFamily,
                   String specificProtocol, UsbService usbService, Activity act,
                   OutputStreamWriter fOut, OutputStreamWriter fOutLog, String myIp) {

        super(nFrets, strings, name, polyphony, OscAddress, receivePort, typeFamily, specificProtocol, myIp);
        
        //this.portControl = new PortControl("COM8",9600);
        //this.portControl = null;
        TextView txtLog = (TextView) act.findViewById(R.id.textViewLog);
        this.activity = act;
        //imprimir inicio do log com data e hora
        DateFormat df = new SimpleDateFormat("dd MM yyyy, HH:mm");
        String date = df.format(Calendar.getInstance().getTime());

        txtLog.append("---- ROBOMUS LOG "+ date+ " ----\n");

        //fim imprimir log
        try {

            this.receiver = new OSCPortIn(this.receivePort);
        }
        catch (SocketException ex) {
            Logger.getLogger(MyRobot.class.getName()).log(Level.SEVERE, null, ex);

        }
        this.fOut = fOut;
        this.fOutLog = fOutLog;
        this.usbService = usbService;

        this.buffer = new Buffer(this.activity, usbService, fOut, fOutLog);

    }
   
    public void handshake(){

        List args = new ArrayList<>();
        
        //instrument attributes
        args.add(this.name);
        args.add(this.polyphony);
        args.add(this.typeFamily);
        args.add(this.specificProtocol);
        args.add(this.myOscAddress);
        args.add(this.myIp);
        args.add(this.receivePort);
  
        //amount of attributes
        args.add(2);
        //fretted instrument attributs
        args.add(this.nFrets); //number of frets
        args.add(convertInstrumentoStringToString()); // strings and turnings

      
	    OSCMessage msg = new OSCMessage("/handshake", args);
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
        this.serverOscAddress = message.getArguments().get(0).toString();
        this.severIpAddress = message.getArguments().get(1).toString();
        this.sendPort = Integer.parseInt(message.getArguments().get(2).toString());
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
        //Initializing the OSC Receiver
        this.receiver = null;


    }

    public void listenThread(){
        System.out.println("Inicio "+ this.receivePort+" ad "+this.myOscAddress);

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
       /*
    public void msgToArduino(){
    byte[] TstArray= new byte[1];
    TstArray[0]=47;
        try {
            portControl.sendData(TstArray);
        } catch (IOException ex) {
            Logger.getLogger(MyRobot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/
    
    /*
    public static void main(String[] args) {
        
        ArrayList<InstrumentString> l = new ArrayList();
        l.add(new InstrumentString(0, "A"));
        l.add(new InstrumentString(0, "B"));
        String specificP = "</slide;posicaoInicial_int><hgyuiyugyu>";
        
        try {
            MyRobot myRobot = new MyRobot(12, l, "laplap", 6, "/laplap", InetAddress.getByName("192.168.1.232"),
                    12345, 1234, "Fretted", specificP);
            
            myRobot.handshake();
            myRobot.listenThread();
            myRobot.msgToArduino();
            //Thread thread = new Thread("bufferProcess");
        } catch (UnknownHostException ex) {
            Logger.getLogger(MyRobot.class.getName()).log(Level.SEVERE, null, ex);
        } 
        

    } */


        
    
}
