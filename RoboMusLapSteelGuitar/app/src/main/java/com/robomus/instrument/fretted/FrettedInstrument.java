/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robomus.instrument.fretted;

import com.robomus.instrument.Instrument;

import java.util.ArrayList;

/**
 *
 * @author Higor
 */
public abstract class FrettedInstrument extends Instrument {
    
    protected int nFrets;
    protected ArrayList<InstrumentString> strings;

    public FrettedInstrument(int nFrets, ArrayList<InstrumentString> strings, String name,
                             int polyphony, String OscAddress, int receivePort, String typeFamily, String specificProtocol, String myIp) {
        super(name, polyphony, OscAddress, receivePort,
                typeFamily, specificProtocol, myIp);
        
        this.nFrets = nFrets;
        this.strings = strings;
    }
    
    public String convertInstrumentoStringToString(){
        String formated = "";
        
        for (InstrumentString s : strings) {
            String aux = "<"+Integer.toString(s.getStringNumber())+ ";"+ s.getOpenStringNote()+">";
            formated = formated + aux;
        }
        return formated;
    }
    
    
}
