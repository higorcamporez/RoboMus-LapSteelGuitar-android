package com.robomus.instrument.fretted.lapsteelguitar;

import android.util.Log;

import com.illposed.osc.OSCMessage;
import com.robomus.arduinoCommunication.UsbService;

/**
 * Created by Higor on 10/11/2016.
 */
public abstract class RobotAction extends Thread{
    UsbService usbService;

    public RobotAction(UsbService usbService) {
        this.usbService = usbService;
    }
    /*
    * Method to play a specific string
    * Format OSC= [timeSleep, id, string]
    * Message to Arduino:  action Arduino code (30), string, action server id
    */
    public void playString(OSCMessage oscMessage) {
        Log.i("playString", "Chegou");
        //byte idArduino = Byte.parseByte(oscMessage.getArguments().get(1).toString());
        byte string = Byte.parseByte(oscMessage.getArguments().get(2).toString());
        byte[] data = {30, string, 100};
        usbService.write(data);

    }
    /*
    method to move the bar to a specific position
    Format OSC = [timestamp, id, fretPosition]
    Message to Arduino:  action Arduino code (60), fretPosition, action server id
    */
    public void positionBar(OSCMessage oscMessage) {
        byte fret = Byte.parseByte(oscMessage.getArguments().get(2).toString());
        byte idArduino = Byte.parseByte(oscMessage.getArguments().get(1).toString());
        byte[] data = {60, fret, idArduino};
        usbService.write(data);
        Log.i("action","position bar, fret="+fret);

    }
    /*
    method to move bar up or down
    Format OSC = [timestamp, id, position] position = 0 -> down, 1-> up
    Message to Arduino:  action Arduino code (50), position, action server id
    */
    public void moveBar(OSCMessage oscMessage) {
        byte posicao = Byte.parseByte(oscMessage.getArguments().get(2).toString());
        byte idArduino = Byte.parseByte(oscMessage.getArguments().get(1).toString());
        byte[] data = {50, posicao, idArduino};
        usbService.write(data);

    }
    /*
    method to slide bar
    Format OSC = [timestamp, id, start position, end position ]
    Message to Arduino:  action Arduino code (00), position, action server id
    */
    public void slide(OSCMessage oscMessage) {
        byte startPosition = Byte.parseByte(oscMessage.getArguments().get(2).toString());
        byte endPosition = Byte.parseByte(oscMessage.getArguments().get(2).toString());
        byte idArduino = Byte.parseByte(oscMessage.getArguments().get(1).toString());
        byte[] data = {0, startPosition, endPosition, idArduino, };
        usbService.write(data);

    }


}