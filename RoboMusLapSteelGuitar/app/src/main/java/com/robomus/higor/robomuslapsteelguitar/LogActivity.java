package com.robomus.higor.robomuslapsteelguitar;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.format.Formatter;
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


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;


public class LogActivity extends AppCompatActivity {

    FileOutputStream fOutLogBlink = null;
    FileOutputStream fOutLog = null;
    OutputStreamWriter myOutWriterLog =null;
    OutputStreamWriter myOutWriterLogBlink =null;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

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
        //search the ip adress
        WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        final String ipAddress = Formatter.formatIpAddress(ip);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);


        //file txt to debug
        verifyStoragePermissions(this);
        File path = new File(Environment.getExternalStorageDirectory() + "/Download");
        File myFile = new File(path, "RoboMus-debug-blink-"+System.currentTimeMillis()+".txt");
        File myFileLog = new File(path, "RoboMus-debug-log"+System.currentTimeMillis()+".txt");

        try {
            fOutLogBlink = new FileOutputStream(myFile,true);
            fOutLog = new FileOutputStream(myFileLog,true);
            myOutWriterLogBlink = new OutputStreamWriter(fOutLogBlink);
            myOutWriterLog = new OutputStreamWriter(fOutLog);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "ng1", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "ng2", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        //display = (TextView) findViewById(R.id.textView1);
        //editText = (EditText) findViewById(R.id.editText1);
        Button sendButton = (Button) findViewById(R.id.button2);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    byte[] data = {0};
                    if (usbService != null) {
                        usbService.write(data);
                        Toast.makeText(getApplicationContext(),"enviou", Toast.LENGTH_SHORT).show();
                    }else{

                        Toast.makeText(getApplicationContext(),"usb null", Toast.LENGTH_SHORT).show();
                    }

            }
        });
        Button startButton = (Button) findViewById(R.id.buttonStart);
        final Activity thisActivity = this;
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
                String specificP = "</slidee;posicaoInicial_int><hgyuiyugyu>";




                MyRobot myRobot = null;

                if(check == 0){

                        myRobot = new MyRobot(12, l, "laplap", 6, oscServerAdress, oscInstrumentAdress , ip,
                                port, 1234, "Fretted", specificP, null, thisActivity, myOutWriterLogBlink, myOutWriterLog, ipAddress);
                        myRobot.listenThread();
                        myRobot.handshake();

                }else {


                    byte[] data = {1};
                    if (usbService != null) {


                            myRobot = new MyRobot(12, l, "laplap", 6, oscServerAdress, oscInstrumentAdress , ip,
                                    port, 1234, "Fretted", specificP, usbService, thisActivity, myOutWriterLogBlink, myOutWriterLog, ipAddress);
                            myRobot.listenThread();
                            myRobot.handshake();

                    } else {

                        Toast.makeText(getApplicationContext(), "Conect the arduino first", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        Button stopButton = (Button) findViewById(R.id.buttonStop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    myOutWriterLogBlink.close();
                    fOutLogBlink.close();

                    myOutWriterLog.close();
                    fOutLog.close();

                    Toast.makeText(getApplicationContext(), "close file", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
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
                    //mActivity.get().display.append(data);
                    break;
            }
        }
    }
}
