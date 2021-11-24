package com.example.led_control;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public MainActivity() {}

    private static final String TAG = "LED-ControlAPP";
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    private final static int REQUEST_ENABLE_BT = 1;

    private Button searchBtn;
    private Button connectBtn;
    private LinearLayout bleDevicesLinearLayout;
    private BluetoothDevice device;

    private BluetoothLeScanner bleScanner;
    private BluetoothGatt bluetoothGatt;
    private int ConnectionState = STATE_DISCONNECTED;
    private final UUID UUID_Test = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");

    private final Handler handler = new Handler();
    private static final long INTERVAL = 5000;

    private int deviceCounter = 0;
    private final Map<Integer, BluetoothDevice> devices = new HashMap<>();

    private final ScanCallback leScanCallback = new ScanCallback() {
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
        connectBtn = findViewById(R.id.ConnectBtn);

        bleDevicesLinearLayout = findViewById(R.id.bleDevicesLinearLayout);

        if (!hasBlePermissions(this)) {
            requestBlePermissions(this, 1);
        }

        areLocationServicesEnabled(this);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bleScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        searchBtn.setOnClickListener(v -> {
            resetFoundDevices();
            Log.i(TAG, "Suche nach BLE Geräte > beginn");
            searchBtn.setEnabled(false);

            AsyncTask.execute(() -> bleScanner.startScan(leScanCallback));

            handler.postDelayed(() -> {
                Log.i(TAG, "Suche nach BLE Geräte > ende");
                searchBtn.setEnabled(true);
                AsyncTask.execute(() -> bleScanner.stopScan(leScanCallback));
            }, INTERVAL);
        });

        //BluetoothDevice device = devices.get(0);//TODO nicht erstes sondern ausgewähltes device
        //connectBtn.setEnabled(device != null); //TODO bei ausgwählen des devices wieder aufrufen
        connectBtn.setText("Verbinden");
        connectBtn.setOnClickListener(v -> {
            if (connectBtn.getText().equals("Verbinden")) {
                device = devices.get(0);//TODO remove später
                bluetoothGatt = device.connectGatt(this,false, serverCallback);
                connectBtn.setText("Trennen");
            }
            else {
                device = null;
                connectBtn.setText("Verbinden");
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

    public boolean hasBlePermissions(Context mContext) {
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

    BluetoothGattCallback serverCallback = new BluetoothGattCallback() {
        @Override public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                ConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        bluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                ConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
                close();
            }
        }
        @Override public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
        @Override public void onCharacteristicRead(BluetoothGatt gatt,
                                                   BluetoothGattCharacteristic characteristic,
                                                   int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(characteristic);
            }
        }
        @Override public void onCharacteristicWrite(BluetoothGatt gatt,
                                                           BluetoothGattCharacteristic characteristic,
                                                           int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(characteristic);
            }
        }

        //@Override public void writeDescriptor() {

        //}

        //@Override public void onCharacteristicChanged() {

        //}

    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(MainActivity.ACTION_DATA_AVAILABLE);

        // Dies ist eine einzigartige Datenanalysemethode für das Profil des Herzfrequenzmessgeräts
        if (UUID_Test.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // Konvertieren Sie für andere Profile die Daten in hexadezimal
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                        stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
        ConnectionState = STATE_DISCONNECTED;
    }
}