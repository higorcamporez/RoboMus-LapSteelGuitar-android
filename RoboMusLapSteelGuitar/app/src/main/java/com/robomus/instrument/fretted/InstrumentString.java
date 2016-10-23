/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robomus.instrument.fretted;

/**
 *
 * @author Higor
 */
public class InstrumentString {
    private int stringNumber;
    private String openStringNote;

    public InstrumentString(int stringNumber, String openStringNote) {
        this.stringNumber = stringNumber;
        this.openStringNote = openStringNote;
    }

    public int getStringNumber() {
        return stringNumber;
    }

    public void setStringNumber(int stringNumber) {
        this.stringNumber = stringNumber;
    }

    public String getOpenStringNote() {
        return openStringNote;
    }

    public void setOpenStringNote(String openStringNote) {
        this.openStringNote = openStringNote;
    }   
}
