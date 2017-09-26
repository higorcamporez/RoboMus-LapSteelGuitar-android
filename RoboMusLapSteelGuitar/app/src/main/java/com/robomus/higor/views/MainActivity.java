package com.robomus.higor.views;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


import com.robumus.higor.robomuslapsteelguitar.R;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        Button bSend = (Button) findViewById(R.id.button);
        final CheckBox checkArduino = (CheckBox) findViewById(R.id.checkBoxArduino);


        bSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this, LogActivity.class);
                EditText etPort = (EditText) findViewById(R.id.editTextPort);
                EditText etInstrument = (EditText) findViewById(R.id.editTextInstrument);
                it.putExtra("port", etPort.getText().toString());
                it.putExtra("instrument", etInstrument.getText().toString());
                if(checkArduino.isChecked()){
                    it.putExtra("arduino", "1");
                }else{
                    it.putExtra("arduino", "0");
                }

                startActivity(it);
            }
        });

        //WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String ipAddress = Formatter.formatIpAddress(ip);
        Toast.makeText(getApplicationContext(), "ip = "+ ipAddress, Toast.LENGTH_LONG).show();
        Log.i("ip",ipAddress);
        /*
        OscServer server = null;

        try {
            server = new OscServer(1234);
            OscListener listener = new OscListener() {

                @Override
                public void handleMessage(OscMessage msg) {
                    Toast.makeText(getApplicationContext(), "chegou", Toast.LENGTH_LONG).show();

                    List l = msg.getArguments();
                    for (Object l1 : l) {
                        Toast.makeText(getApplicationContext(), "object=" + l1, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void handleBundle(OscBundle bundle) {

                }


            };
            server.start();
            server.addOscListener(listener);

        } catch (IOException e) {
            e.printStackTrace();
        }



        */

        StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)
                .permitDiskWrites()
                .build());
        StrictMode.setThreadPolicy(old);
        /*
        OSCPortIn receiver = null;

        Toast.makeText(getApplicationContext(), "portin", Toast.LENGTH_LONG).show();

        try {
            receiver = new OSCPortIn(1234);
            if(receiver == null){
                Toast.makeText(getApplicationContext(), "deu ruim", Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(getApplicationContext(), "entrou", Toast.LENGTH_LONG).show();
                OSCListener listener = new OSCListener() {

                    public void acceptMessage(java.util.Date time, OSCMessage message) {
                        //Toast.makeText(getApplicationContext(), "chegou", Toast.LENGTH_LONG).show();

                        List l = message.getArguments();
                        for (Object l1 : l) {
                            //Toast.makeText(getApplicationContext(), "object=" + l1, Toast.LENGTH_LONG).show();
                            Log.e("erro","object=" + l1 );
                        }
                        //verificar se a mensagem é válida

                    }
                };
                receiver.addListener("/laplap/*", listener);
                receiver.startListening();
                if(receiver.isListening()){
                    Toast.makeText(getApplicationContext(), "ta rodando", Toast.LENGTH_LONG).show();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "catch", Toast.LENGTH_LONG).show();
        }
        */
    }
}
