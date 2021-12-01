package com.example.led_control.btconnect;

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
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.led_control.MainActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BTConnectPresenter implements BTConnectContract.Presenter {
    private final MainActivity mainActivity;
    private BTConnectFragment btConnectFragment;

    private static final String TAG = "LED-ControlAPP";
    // Stops scanning after 5 seconds.
    private BluetoothGattCharacteristic charac;

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    BluetoothGatt bluetoothGatt;

    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.M)
    public BTConnectPresenter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        btManager = (BluetoothManager) mainActivity.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
    }

    @Override
    public void setView(BTConnectFragment btConnectFragment) {
        this.btConnectFragment = btConnectFragment;
    }

    @Override
    public void startScan() {
        devicesDiscovered.clear();
        AsyncTask.execute(() -> btScanner.startScan(leScanCallback));
    }

    @Override
    public void stopScan() {
        AsyncTask.execute(() -> btScanner.stopScan(leScanCallback));
    }

    // Device scan callback.
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();



            if (!devicesDiscovered.contains(device)) {
                btConnectFragment.showDevice(device);
                devicesDiscovered.add(result.getDevice());
            }
        }
    };

    // Device connect call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            //MainActivity.this.runOnUiThread(() -> textView.append("device read or wrote to\n"));
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case 0:
                    // neue methode -> fragment
                   /*MainActivity.this.runOnUiThread(() -> {
                        textView.append("device disconnected\n");
                        connectToDevice.setVisibility(View.VISIBLE);
                        disconnectDevice.setVisibility(View.INVISIBLE);
                    });
                    break;*/
                case 2:
                    /*MainActivity.this.runOnUiThread(() -> {
                        textView.append("device connected\n");
                        connectToDevice.setVisibility(View.INVISIBLE);
                        disconnectDevice.setVisibility(View.VISIBLE);
                    });

                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();
*/
                    break;
                default:
                    //MainActivity.this.runOnUiThread(() -> textView.append("we encounterned an unknown state, uh oh\n"));
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
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

    public boolean connectToDeviceSelected(BluetoothDevice device) {
        bluetoothGatt = device.connectGatt(mainActivity.getApplicationContext(), false, btleGattCallback);
        return bluetoothGatt != null;
    }

    public void disconnectDeviceSelected() {
        bluetoothGatt.disconnect();
    }

    /*private void btnclick() {
        writeCharacteristic(charac, "off");
    }*/

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
            //MainActivity.this.runOnUiThread(() -> textView.append("Service disovered: "+uuid+"\n"));
            new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                charac = gattCharacteristic;
                final String charUuid = gattCharacteristic.getUuid().toString();
                System.out.println("Characteristic discovered for service: " + charUuid);
                //MainActivity.this.runOnUiThread(() -> textView.append("Characteristic discovered for service: "+charUuid+"\n"));

            }
        }
    }
}
