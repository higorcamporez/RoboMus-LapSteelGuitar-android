/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robomus.instrument.fretted;

import com.robomus.instrument.Instrument;
import com.robomus.util.Note;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Higor
 */
public abstract class FrettedInstrument extends Instrument {
    
    protected int nFrets;
    protected List<InstrumentString> instrumentStrings;

    public FrettedInstrument( String name, String OscAddress, int receivePort, String myIp) {
        super(name, OscAddress, receivePort, myIp);
        

    }
    
    public String convertInstrumentoStringToString(){
        String formated = "";
        
        for (InstrumentString s : instrumentStrings) {
            String aux = "<"+Integer.toString(s.getStringNumber())+ ";"+ s.getOpenStringNote().toString()+">";
            formated = formated + aux;
        }
        return formated;
    }

    public List<FrettedNotePosition> getNotePositions(Note note){
        List<FrettedNotePosition> l = new ArrayList<>();
        for (InstrumentString instrString: instrumentStrings) {

            Integer fret = instrString.getFret(note);
            if(fret > 1 && fret < this.nFrets ){
                l.add(new FrettedNotePosition(fret, instrString.getStringNumber()));
            }

        }
        return l;
    }
    
}
