package com.example.led_control;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public MainActivity() {}

    private static final String TAG = "LED-ControlAPP";

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private final Handler handler = new Handler();
    // Stops scanning after 5 seconds.
    private static final long SCAN_PERIOD = 5000;
    private BluetoothGattCharacteristic charac;

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    BluetoothGatt bluetoothGatt;

    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<>();
    int deviceIndex = 0;
    TextView textView;

    Button connectToDevice;
    Button disconnectDevice;
    Button startScanningButton;
    Button stopScanningButton;
    EditText deviceIndexInput;

    Button btn;


        private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();

            if (!devicesDiscovered.contains(device)) {
                textView.append("Index: " + deviceIndex + ", Device Name: " + result.getDevice().getName() + " rssi: " + result.getRssi() + "\n");
                devicesDiscovered.add(result.getDevice());
                deviceIndex++;
            }
            // auto scroll for text view
            final int scrollAmount = textView.getLayout().getLineTop(textView.getLineCount()) - textView.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0) {
                textView.scrollTo(0, scrollAmount);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.TextView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        deviceIndexInput = findViewById(R.id.InputIndex);
        deviceIndexInput.setText("0");

        connectToDevice = findViewById(R.id.ConnectButton);
        connectToDevice.setOnClickListener(v -> connectToDeviceSelected());

        btn = findViewById(R.id.button);
        btn.setOnClickListener(v -> btnclick());

        disconnectDevice = findViewById(R.id.DisconnectButton);
        disconnectDevice.setVisibility(View.INVISIBLE);
        disconnectDevice.setOnClickListener(v -> disconnectDeviceSelected());

        startScanningButton = findViewById(R.id.StartScanButton);
        //startScanningButton.setOnClickListener(v -> startScanning());

        stopScanningButton = findViewById(R.id.StopScanButton);
        //stopScanningButton.setOnClickListener(v -> stopScanning());
        stopScanningButton.setVisibility(View.INVISIBLE);

        if (!hasBlePermissions(this) ) {
            requestBlePermissions(this, 1);
        }

        areLocationServicesEnabled(this);

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        startScanningButton.setOnClickListener(v -> {
            deviceIndex = 0;
            devicesDiscovered.clear();
            textView.setText("");
            Log.i(TAG, "Suche nach BLE Geräte > beginn");
            textView.append("Started Scanning\n");
            startScanningButton.setEnabled(false);

            AsyncTask.execute(() -> btScanner.startScan(leScanCallback));

            handler.postDelayed(() -> {
                Log.i(TAG, "Suche nach BLE Geräte > ende");
                textView.append("Stop Scanning\n");
                startScanningButton.setEnabled(true);
                AsyncTask.execute(() -> btScanner.stopScan(leScanCallback));
            }, SCAN_PERIOD);
        });

        //BluetoothDevice device = devices.get(0);//TODO nicht erstes sondern ausgewähltes device
        //connectBtn.setEnabled(device != null); //TODO bei ausgwählen des devices wieder aufrufen
        connectToDevice.setText("Verbinden");
        connectToDevice.setOnClickListener(v -> {
            BluetoothDevice device;
            if (connectToDevice.getText().equals("Verbinden")) {
                device = devicesDiscovered.get(0);//TODO remove später
                bluetoothGatt = device.connectGatt(this,false, serverCallback);
                connectToDevice.setText("Trennen");
            }
            else {
                device = null;
                connectToDevice.setText("Verbinden");
            }
        });
    }

    private void resetFoundDevices() {
        textView.setText("");
        devicesDiscovered.clear();
        deviceIndex = 0;
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

    public void connectToDeviceSelected() {
        textView.append("Trying to connect to device at index: " + deviceIndexInput.getText() + "\n");
        int deviceSelected = Integer.parseInt(deviceIndexInput.getText().toString());
        bluetoothGatt = devicesDiscovered.get(deviceSelected).connectGatt(this, false, serverCallback);
    }

    public void disconnectDeviceSelected() {
        textView.append("Disconnecting from device\n");
        bluetoothGatt.disconnect();
    }

    private void btnclick() {
        writeCharacteristic(charac, "off");
    }

    BluetoothGattCallback serverCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case 0:
                    MainActivity.this.runOnUiThread(() -> {
                        textView.append("device disconnected\n");
                        connectToDevice.setVisibility(View.VISIBLE);
                        disconnectDevice.setVisibility(View.INVISIBLE);
                    });
                    break;
                case 2:
                    MainActivity.this.runOnUiThread(() -> {
                        textView.append("device connected\n");
                        connectToDevice.setVisibility(View.INVISIBLE);
                        disconnectDevice.setVisibility(View.VISIBLE);
                    });

                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();

                    break;
                default:
                    MainActivity.this.runOnUiThread(() -> textView.append("we encounterned an unknown state, uh oh\n"));
                    break;
            }
        }
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a 			BluetoothGatt.discoverServices() call
            MainActivity.this.runOnUiThread(() -> textView.append("device services have been discovered\n"));
            displayGattServices(bluetoothGatt.getServices());
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

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            MainActivity.this.runOnUiThread(() -> textView.append("device read or wrote to\n"));
        }

    };

    private void broadcastUpdate(final BluetoothGattCharacteristic characteristic) {

        System.out.println(characteristic.getUuid());
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            final String uuid = gattService.getUuid().toString();
            System.out.println("Service discovered: " + uuid);
            MainActivity.this.runOnUiThread(() -> textView.append("Service disovered: " + uuid + "\n"));
            new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                charac = gattCharacteristic;
                final String charUuid = gattCharacteristic.getUuid().toString();
                System.out.println("Characteristic discovered for service: " + charUuid);
                MainActivity.this.runOnUiThread(() -> textView.append("Characteristic discovered for service: " + charUuid + "\n"));

            }
        }
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic,
                                    String data) {
        if (btAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        Log.i(TAG, "characteristic " + characteristic.toString());
        try {
            Log.i(TAG, "data " + URLEncoder.encode(data, "utf-8"));

            characteristic.setValue(URLEncoder.encode(data, "utf-8"));

            // TODO
            bluetoothGatt.writeCharacteristic(characteristic);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }
}