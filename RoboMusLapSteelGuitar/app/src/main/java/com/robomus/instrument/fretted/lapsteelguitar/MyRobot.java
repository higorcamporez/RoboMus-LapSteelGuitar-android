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
    OSCPortOut sender;
    OSCPortIn receiver;
    //private PortControl portControl;
    
    public MyRobot(int nFrets, ArrayList<InstrumentString> strings, String name,
                   int polyphony, String serverOscAddress, String OscAddress, String severAddress,
                   int sendPort, int receivePort, String typeFamily, String specificProtocol,
                   UsbService usbService, Activity act, OutputStreamWriter fOut, OutputStreamWriter fOutLog, String myIp) {

        super(nFrets, strings, name, polyphony, serverOscAddress, OscAddress, severAddress,
                sendPort, receivePort, typeFamily, specificProtocol, myIp);
        
        //this.portControl = new PortControl("COM8",9600);
        //this.portControl = null;
        TextView txtLog = (TextView) act.findViewById(R.id.textViewLog);

        //imprimir inicio do log com data e hora
        DateFormat df = new SimpleDateFormat("dd MM yyyy, HH:mm");
        String date = df.format(Calendar.getInstance().getTime());

        txtLog.append("---- ROBOMUS LOG "+ date+ " ----\n");

        //fim imprimir log

        try {
            this.buffer = new Buffer(act, InetAddress.getByName(severAddress), serverOscAddress, sendPort, usbService, fOut, fOutLog);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.buffer.start();

        Log.d("myrobot","issoa eaeae");

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

        try {

            this.receiver = new OSCPortIn(this.receivePort);
        }
        catch (SocketException ex) {
            Logger.getLogger(MyRobot.class.getName()).log(Level.SEVERE, null, ex);

        }
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

      
	OSCMessage msg = new OSCMessage(this.serverOscAddress+"/handshake/"+this.name, args);
        
             
        try {
            this.sender.send(msg);
            Log.i("hand", this.serverOscAddress+"/handshake/seila");
        } catch (IOException ex) {
            Logger.getLogger(MyRobot.class.getName()).log(Level.SEVERE, null, ex);
        }
                       
    }
    
    public void listenThread(){
        System.out.println("Inicio "+ this.receivePort+" ad "+this.myOscAddress);

        OSCListener listener = new OSCListener() {
            
            public void acceptMessage(java.util.Date time, OSCMessage message) {
                Log.d("buffer","recebeu msg");
                List l = message.getArguments();
                for (Object l1 : l) {
                   Log.d("buffer","object=" + l1);
                }
                //verificar se a mensagem é válida

                buffer.add(message);
                //buffer.print();
            }
        };
        this.receiver.addListener(this.myOscAddress+"/*", listener);
        this.receiver.startListening();
        
    }
    public void ConfirmMsgToServ(){

        List args = new ArrayList<>();
        args.add("action completed");
        
        OSCMessage msg = new OSCMessage("/handshake", args);
             
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
            OSCMessage msg = new OSCMessage("/action");
            msg.addArgument(idConveted);

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
