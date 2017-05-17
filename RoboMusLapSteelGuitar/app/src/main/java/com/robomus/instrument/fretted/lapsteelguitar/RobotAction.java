package com.robomus.instrument.fretted.lapsteelguitar;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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
        byte idArduino = convertId( Integer.parseInt(oscMessage.getArguments().get(1).toString() ) );
        byte string = Byte.parseByte(oscMessage.getArguments().get(2).toString());
        byte[] data = {30, string, idArduino};
        usbService.write(data);

    }
    /*
    method to move the bar to a specific position
    Format OSC = [timestamp, id, fretPosition]
    Message to Arduino:  action Arduino code (60), fretPosition, action server id
    */
    public void positionBar(OSCMessage oscMessage) {
        byte fret = Byte.parseByte(oscMessage.getArguments().get(2).toString());
        byte idArduino = convertId( Integer.parseInt(oscMessage.getArguments().get(1).toString() ) );
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
        byte idArduino = convertId( Integer.parseInt(oscMessage.getArguments().get(1).toString() ) );
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
        //byte idArduino = Byte.parseByte(oscMessage.getArguments().get(1).toString());
        byte idArduino = convertId( Integer.parseInt(oscMessage.getArguments().get(1).toString() ) );
        byte[] data = {0, startPosition, endPosition, idArduino, };
        usbService.write(data);

    }

    /* ---------------------- test -----------*/
    public void testMsg(OSCMessage oscMessage){
        byte idArduino = convertId( Integer.parseInt(oscMessage.getArguments().get(1).toString() ) );
        Log.i("id env ard", Integer.toString(idArduino&0xFF)+" - "+oscMessage.getArguments().get(1).toString());
        byte[] data = {100, idArduino };
        usbService.write(data);
    }
    public void playNoteTest(OSCMessage oscMessage) {
        byte fretNumber = Byte.parseByte(oscMessage.getArguments().get(1).toString());
        byte stringNumber = Byte.parseByte(oscMessage.getArguments().get(2).toString());
        byte[] data = {100, fretNumber, stringNumber};
        usbService.write(data);
    }
    /*
    * method to convert the id from server to send to arduino
    * @paran idFromServer the message id received from the server
    */
    public Byte convertId( int idFromServer){
        return (byte)(idFromServer%128);
    }
    public void playSound(OSCMessage oscMessage) {
        System.out.println(oscMessage.getArguments().size()+" [3]"+oscMessage.getArguments().get(3)+" [4]"+oscMessage.getArguments().get(2));
        Integer duration = Integer.parseInt((String)oscMessage.getArguments().get(3));
        final int sampleRate = 8000;
        final int numSamples = (duration) * sampleRate;
        final double sample[] = new double[numSamples];
        Integer freqOfTone = 440; // hz
        byte generatedSnd[] = new byte[2 * numSamples];

        freqOfTone = Integer.parseInt((String)oscMessage.getArguments().get(2));




            // fill out the array
            for (int i = 0; i < numSamples; ++i) {
                sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
            }

            // convert to 16 bit pcm sound array
            // assumes the sample buffer is normalised.
            int idx = 0;
            for (final double dVal : sample) {
                // scale to maximum amplitude
                final short val = (short) ((dVal * 32767));
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

            }

            final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, numSamples,
                    AudioTrack.MODE_STATIC);
            audioTrack.write(generatedSnd, 0, generatedSnd.length);
            audioTrack.play();


    }
}