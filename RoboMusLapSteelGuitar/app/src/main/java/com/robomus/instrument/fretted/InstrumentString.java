/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robomus.instrument.fretted;

import com.robomus.util.Note;

/**
 *
 * @author Higor
 */
public class InstrumentString {
    private int stringNumber;
    private Note openStringNote;

    public InstrumentString(int stringNumber, Note openStringNote) {
        this.stringNumber = stringNumber;
        this.openStringNote = openStringNote;
    }

    public int getStringNumber() {
        return stringNumber;
    }

    public void setStringNumber(int stringNumber) {
        this.stringNumber = stringNumber;
    }

    public Note getOpenStringNote() {
        return openStringNote;
    }

    public void setOpenStringNote(Note openStringNote) {
        this.openStringNote = openStringNote;
    }
    public Integer getFret(Note note){
        return openStringNote.getDistanceTo(note);

    }
}
