package com.robomus.instrument.fretted.lapsteelguitar;

/**
 * Created by Higor on 19/09/2017.
 */

public class ToneBar {

    private Integer fretPosition;
    private Boolean isPressedDown;

    public ToneBar() {
        this.setInitialPosition();
    }

    public ToneBar(Integer fretPosition, Boolean isPressedDown) {
        this.fretPosition = fretPosition;
        this.isPressedDown = isPressedDown;
    }

    public void setInitialPosition(){
        fretPosition = 0;
        isPressedDown = false;
    }
    public Integer getFretPosition() {
        return fretPosition;
    }

    public void setFretPosition(Integer fretPosition) {
        this.fretPosition = fretPosition;
    }

    public Boolean getPressedDown() {
        return isPressedDown;
    }

    public void setPressedDown(Boolean pressedDown) {
        isPressedDown = pressedDown;
    }
}
