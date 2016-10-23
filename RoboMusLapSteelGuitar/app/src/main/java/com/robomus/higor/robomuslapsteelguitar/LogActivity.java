package com.robomus.higor.robomuslapsteelguitar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;


import com.robomus.instrument.fretted.InstrumentString;
import com.robomus.instrument.fretted.lapsteelguitar.MyRobot;
import com.robumus.higor.robomuslapsteelguitar.R;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;



public class LogActivity extends AppCompatActivity {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        Intent it = getIntent();
        String ip = it.getStringExtra("ip");

        int port = Integer.parseInt(it.getStringExtra("port"));
        String oscServerAdress = it.getStringExtra("server");
        String oscInstrumentAdress = it.getStringExtra("instrument");

        ArrayList<InstrumentString> l = new ArrayList<InstrumentString>();
        l.add(new InstrumentString(0, "A"));
        l.add(new InstrumentString(0, "B"));
        String specificP = "</slide;posicaoInicial_int><hgyuiyugyu>";

        View view = findViewById(R.id.view);
        MyRobot myRobot = null;

        try {
            myRobot = new MyRobot(12, l, "laplap", 6,oscServerAdress, oscInstrumentAdress+"/*", InetAddress.getByName(ip),
                    port, 1234, "Fretted", specificP, view, this);
            myRobot.listenThread();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        //myRobot.handshake();


    }
}
