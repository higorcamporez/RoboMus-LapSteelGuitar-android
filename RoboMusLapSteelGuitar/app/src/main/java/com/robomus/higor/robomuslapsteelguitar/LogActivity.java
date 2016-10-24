package com.robomus.higor.robomuslapsteelguitar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.robomus.arduinoCommunication.UsbService;
import com.robomus.instrument.fretted.InstrumentString;
import com.robomus.instrument.fretted.lapsteelguitar.MyRobot;
import com.robumus.higor.robomuslapsteelguitar.R;


import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;



public class LogActivity extends AppCompatActivity {

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private UsbService usbService;
    private TextView display;
    private EditText editText;
    private MyHandler mHandler;

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_log);
        mHandler = new MyHandler(this);

        //display = (TextView) findViewById(R.id.textView1);
        //editText = (EditText) findViewById(R.id.editText1);
        Button sendButton = (Button) findViewById(R.id.button2);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    byte[] data = {1};
                    if (usbService != null) {
                        usbService.write(data);
                        Toast.makeText(getApplicationContext(),"enviou", Toast.LENGTH_SHORT).show();
                    }else{

                        Toast.makeText(getApplicationContext(),"usb null", Toast.LENGTH_SHORT).show();
                    }

            }
        });
        Button startButton = (Button) findViewById(R.id.buttonStart);
        final Activity a = this;
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent it = getIntent();
                String ip = it.getStringExtra("ip");

                int port = Integer.parseInt(it.getStringExtra("port"));
                String oscServerAdress = it.getStringExtra("server");
                String oscInstrumentAdress = it.getStringExtra("instrument");
                int check = Integer.parseInt(it.getStringExtra("arduino"));

                ArrayList<InstrumentString> l = new ArrayList<InstrumentString>();
                l.add(new InstrumentString(0, "A"));
                l.add(new InstrumentString(0, "B"));
                String specificP = "</slide;posicaoInicial_int><hgyuiyugyu>";

                MyRobot myRobot = null;

                if(check == 0){
                    try {
                        myRobot = new MyRobot(12, l, "laplap", 6, oscServerAdress, oscInstrumentAdress + "/*", InetAddress.getByName(ip),
                                port, 1234, "Fretted", specificP, null, a);
                        myRobot.listenThread();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }else {


                    byte[] data = {1};
                    if (usbService != null) {

                        try {
                            myRobot = new MyRobot(12, l, "laplap", 6, oscServerAdress, oscInstrumentAdress + "/*", InetAddress.getByName(ip),
                                    port, 1234, "Fretted", specificP, usbService, a);
                            myRobot.listenThread();
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                    } else {

                        Toast.makeText(getApplicationContext(), "Conect the arduino first", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });








    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<LogActivity> mActivity;

        public MyHandler(LogActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    mActivity.get().display.append(data);
                    break;
            }
        }
    }
}
