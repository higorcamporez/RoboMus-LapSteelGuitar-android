package com.robomus.instrument.fretted.lapsteelguitar;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.illposed.osc.OSCMessage;
import com.robomus.arduinoCommunication.UsbService;
import com.robomus.instrument.fretted.FrettedNotePosition;
import com.robomus.util.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Higor on 10/11/2016.
 */
public abstract class RobotAction extends Thread{

    private UsbService usbService;
    private MyRobot myRobot;

    public RobotAction(MyRobot myRobot) {
        this.usbService = myRobot.getUsbService();
        this.myRobot = myRobot;
    }
    /*
    * Method to put a robot in initial position. tone bar raised and in fret 0
    * Format OSC= [id]
    * Message to Arduino:  action Arduino code (XX)
    */
    public void initialRobotPosition() {
        Log.i("initialRobotPosition", "-");
        byte[] data = {1};
        usbService.write(data);
        myRobot.getToneBar().setInitialPosition();
    }
    /*
    /*
    * Method to play a specific string
    * Format OSC= [timeSleep, id, string]
    * Message to Arduino:  action Arduino code (30), string, action server id
    */
    public void playString(OSCMessage oscMessage) {
        Log.i("RobotAction", "playString() - Chegou");
        byte idMsg = convertId( Long.parseLong(oscMessage.getArguments().get(1).toString() ) );
        byte string = Byte.parseByte(oscMessage.getArguments().get(2).toString());
        byte[] data = {30, string, idMsg};
        usbService.write(data);


    }
    /*
    method to move the bar to a specific position
    Format OSC = [timestamp, id, fretPosition]
    Message to Arduino:  action Arduino code (60), fretPosition, action server id
    */
    public void positionBar(OSCMessage oscMessage) {
        byte fret = Byte.parseByte(oscMessage.getArguments().get(2).toString());
        byte idMsg = convertId( Long.parseLong(oscMessage.getArguments().get(1).toString() ) );
        byte[] data = {60, fret, idMsg};
        usbService.write(data);
        Log.i(this.getName(),"positionBar() - fret="+fret);
    }
    /*
    method to move bar up or down
    Format OSC = [timestamp, id, position] position = 0 -> down, 1-> up
    Message to Arduino:  action Arduino code (50), position, action server id
    */
    public void moveBar(OSCMessage oscMessage) {
        byte posicao = Byte.parseByte(oscMessage.getArguments().get(2).toString());
        byte idArduino = convertId( Long.parseLong(oscMessage.getArguments().get(1).toString() ) );
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
        byte idArduino = convertId( Long.parseLong(oscMessage.getArguments().get(1).toString() ) );
        byte[] data = {0, startPosition, endPosition, idArduino, };
        usbService.write(data);

    }
    /*
    method to play a instrumentString with the press fret
    Format OSC = [timestamp, id, instrumentString, fret ]
    Message to Arduino:  action Arduino code (00), position, action server id
    */
    public void playNote(String id, FrettedNotePosition frettedNotePosition){

        byte instrumentString = frettedNotePosition.getInstrumentString().byteValue();
        byte fret =  frettedNotePosition.getFret().byteValue();

        byte idArduino = convertId( Long.parseLong(id) );
        byte[] data = {65, instrumentString, fret, idArduino };
        usbService.write(data);
    }

    /* ---------------------- test -----------*/
    public void testMsg(OSCMessage oscMessage){
        byte idArduino = convertId( Long.parseLong(oscMessage.getArguments().get(1).toString() ) );
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
    public Byte convertId( Long idFromServer){
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

    public void playNote(OSCMessage oscMessage){

        Long idMessage = Long.parseLong(oscMessage.getArguments().get(0).toString());
        Short relativeTime = Short.parseShort(oscMessage.getArguments().get(1).toString());
        Short duration = Short.parseShort(oscMessage.getArguments().get(2).toString());
        String symbolNote = oscMessage.getArguments().get(3).toString();

        Log.i(this.getName(),"playNote: rt="+relativeTime);
        Note note = new Note(symbolNote);

        FrettedNotePosition notePosition = this.myRobot.getNoteClosePosition(note);

        if(notePosition != null){
            if(myRobot.getEmulate()) { // verifica se é emulacao. vai emitir som no celular
                playSoundSmartPhone(note.getFrequency(), duration);
            }else{

                byte lowRelativeTime =  (byte)(relativeTime&0xFF);
                byte highRelativeTime = (byte)(relativeTime>>8);
                byte lowDuration = (byte)(duration&0xFF);
                byte highDuration = (byte)(duration>>8);

                byte instrumentString = notePosition.getInstrumentString().byteValue();
                byte fret =  notePosition.getFret().byteValue();

                byte idMsgArduino = convertId( idMessage );
                byte[] data = { 65, idMsgArduino, highRelativeTime, lowRelativeTime, highDuration,
                                lowDuration, instrumentString, fret  };

                usbService.write(data);
                Log.i(this.getName(),"playNote: Enviou");

            }
        }else{

            Log.i(this.getName(), "playNote: note "+symbolNote+" not possible");

        }

    }
    public void playSoundSmartPhone(double frequency, double duration){
        final int sampleRate = 8000;
        final int numSamples = (int) (duration/1000) * sampleRate;
        final double sample[] = new double[numSamples];
        byte generatedSnd[] = new byte[2 * numSamples];

        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/frequency));
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

    public void playHappyBirth(OSCMessage oscMessage){
        int delay = 1500;

        List<Note> notes = new ArrayList<>();

        notes.add(new Note("E4"));
        notes.add(new Note("E4"));
        notes.add(new Note("F#4"));
        notes.add(new Note("E4"));
        notes.add(new Note("A4"));
        notes.add(new Note("G#4"));

        notes.add(new Note("E4"));
        notes.add(new Note("E4"));
        notes.add(new Note("F#4"));
        notes.add(new Note("E4"));
        notes.add(new Note("B4"));
        notes.add(new Note("A4"));
        notes.add(new Note("A4"));

        notes.add(new Note("C#5"));
        notes.add(new Note("C#5"));
        notes.add(new Note("E5"));
        notes.add(new Note("C#5"));
        notes.add(new Note("A4"));
        notes.add(new Note("G#4"));
        notes.add(new Note("F#4"));

        notes.add(new Note("D5"));
        notes.add(new Note("D5"));
        notes.add(new Note("C#5"));
        notes.add(new Note("A4"));
        notes.add(new Note("B4"));
        notes.add(new Note("A4"));
        notes.add(new Note("A4"));

        for (Note note: notes) {
            FrettedNotePosition notePosition = this.myRobot.getNoteClosePosition(note);
            if(notePosition != null){
                if(myRobot.getEmulate()) { // verifica se é emulacao. vai emitir som no celular
                    playSoundSmartPhone(note.getFrequency(), delay);
                }else{
                    playNote(oscMessage.getArguments().get(0).toString(), notePosition);
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{

                Log.i("playNote", "playNote: note "+note.getSymbol()+note.getOctavePitch() +" not possible");
            }
        }


    }
}