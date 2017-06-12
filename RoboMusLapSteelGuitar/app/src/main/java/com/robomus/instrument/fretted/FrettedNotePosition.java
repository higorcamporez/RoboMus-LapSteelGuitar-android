package com.robomus.instrument.fretted;

/**
 * Created by Higor on 10/06/2017.
 */

public class FrettedNotePosition {

    private Integer fret;
    private Integer instrumentString;

    public FrettedNotePosition(Integer fret, Integer instrumentString) {
        this.fret = fret;
        this.instrumentString = instrumentString;
    }

    public Integer getFret() {
        return fret;
    }

    public void setFret(Integer fret) {
        this.fret = fret;
    }

    public Integer getInstrumentString() {
        return instrumentString;
    }

    public void setInstrumentString(Integer instrumentString) {
        this.instrumentString = instrumentString;
    }
}
