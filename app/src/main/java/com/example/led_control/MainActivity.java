package com.example.led_control;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LED-ControlAPP";

    private Button searchBtn;

    private LinearLayout bleDevicesLinearLayout;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;

    private final static int REQUEST_ENABLE_BT = 1;

    private final Handler handler = new Handler();
    private static final long INTERVAL = 5000;

    private int deviceCounter = 0;
    private Map<Integer, String> devicesDescription = new HashMap<>();
    private Map<Integer, BluetoothDevice> devices = new HashMap<>();

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();

            if (!devices.containsValue(device)) {
                String text = String.format("%d - %s, %s\r", deviceCounter, device.getName(), device.getAddress());
                TextView textView = new TextView(getActivity());
                textView.setText(text);
                textView.setTag(device.getName());
                bleDevicesLinearLayout.addView(textView);

                devices.put(deviceCounter, device);
                deviceCounter++;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBtn = findViewById(R.id.searchBtn);

        bleDevicesLinearLayout = findViewById(R.id.bleDevicesLinearLayout);

        if ( !hasBlePermissions(this) ) {
            requestBlePermissions(this, 1);
        }

        areLocationServicesEnabled(this);

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bleScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFoundDevices();
                Log.i(TAG, "Suche nach BLE Geräte > beginn");
                searchBtn.setEnabled(false);

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        bleScanner.startScan(leScanCallback);
                    }
                });

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Suche nach BLE Geräte > ende");
                        searchBtn.setEnabled(true);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                bleScanner.stopScan(leScanCallback);
                            }
                        });
                    }
                }, INTERVAL);
            }
        });

    }

    private void resetFoundDevices() {
        bleDevicesLinearLayout.removeAllViews();
        devices.clear();
        deviceCounter = 0;
    }

    public void areLocationServicesEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasBlePermissions( Context mContext) {
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestBlePermissions(final Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                requestCode);
    }

    public MainActivity getActivity() {
        return this;
    }
}