package com.example.led_control;

import android.Manifest;
import android.app.AlertDialog;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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
    Boolean btScanning = false;

    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<>();
    int deviceIndex = 0;
    TextView textView;

    Button connectToDevice;
    Button disconnectDevice;
    Button startScanningButton;
    Button stopScanningButton;
    EditText deviceIndexInput;

    Button btn;


    @RequiresApi(api = Build.VERSION_CODES.M)
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

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(dialog -> requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION));
            builder.show();
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
    }

    // Device scan callback.
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

    // Device connect call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            MainActivity.this.runOnUiThread(() -> textView.append("device read or wrote to\n"));
        }

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

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(characteristic);
            }
        }
    };

    private void broadcastUpdate(final BluetoothGattCharacteristic characteristic) {

        System.out.println(characteristic.getUuid());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ActivityCompat.requestPermissions(
                this,
                new String[]
                        {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        }, 0);

        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("coarse location permission granted");
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Functionality limited");
                builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(dialog -> {
                });
                builder.show();
            }
        }
    }

    /*public void startScanning() {
        System.out.println("start scanning");
        btScanning = true;
        deviceIndex = 0;
        devicesDiscovered.clear();
        textView.setText("");
        textView.append("Started Scanning\n");
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(() -> btScanner.startScan(leScanCallback));

        handler.postDelayed(this::stopScanning, SCAN_PERIOD);
    }*/

    /*public void stopScanning() {
        System.out.println("stopping scanning");
        textView.append("Stopped Scanning\n");
        btScanning = false;
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(() -> btScanner.stopScan(leScanCallback));
    }*/

    public void connectToDeviceSelected() {
        textView.append("Trying to connect to device at index: " + deviceIndexInput.getText() + "\n");
        int deviceSelected = Integer.parseInt(deviceIndexInput.getText().toString());
        bluetoothGatt = devicesDiscovered.get(deviceSelected).connectGatt(this, false, btleGattCallback);
    }

    public void disconnectDeviceSelected() {
        textView.append("Disconnecting from device\n");
        bluetoothGatt.disconnect();
    }

    private void btnclick() {
        writeCharacteristic(charac, "off");
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


    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            final String uuid = gattService.getUuid().toString();
            System.out.println("Service discovered: " + uuid);
            MainActivity.this.runOnUiThread(() -> textView.append("Service disovered: "+uuid+"\n"));
            new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                charac = gattCharacteristic;
                final String charUuid = gattCharacteristic.getUuid().toString();
                System.out.println("Characteristic discovered for service: " + charUuid);
                MainActivity.this.runOnUiThread(() -> textView.append("Characteristic discovered for service: "+charUuid+"\n"));

            }
        }
    }
}