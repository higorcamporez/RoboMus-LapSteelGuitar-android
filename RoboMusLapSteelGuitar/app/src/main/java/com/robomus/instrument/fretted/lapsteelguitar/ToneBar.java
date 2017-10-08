package com.robomus.instrument.fretted.lapsteelguitar;

/**
 * Created by Higor on 19/09/2017.
 */

public class ToneBar {

    private Integer barPosition;
    private Boolean isPressedDown;

    public ToneBar() {
        this.setInitialPosition();
    }

    public ToneBar(Integer fretPosition, Boolean isPressedDown) {
        this.barPosition = fretPosition;
        this.isPressedDown = isPressedDown;
    }

    public void setInitialPosition(){
        barPosition = 0;
        isPressedDown = false;
    }

    public Integer getBarPosition() {
        return barPosition;
    }

    public void setBarPosition(Integer barPosition) {
        this.barPosition = barPosition;
    }

    public Boolean getPressedDown() {
        return isPressedDown;
    }

    public void setPressedDown(Boolean pressedDown) {
        isPressedDown = pressedDown;
    }
}
