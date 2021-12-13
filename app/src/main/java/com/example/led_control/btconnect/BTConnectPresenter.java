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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BTConnectPresenter implements BTConnectContract.Presenter {
    private final MainActivity mainActivity;
    private BTConnectFragment btConnectFragment;

    private static final String TAG = "LED-ControlAPP";
    // Stops scanning after 5 seconds.

    BluetoothGattCharacteristic charac;
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
        if (btAdapter.isEnabled()) {
            devicesDiscovered.clear();
            AsyncTask.execute(() -> btScanner.startScan(leScanCallback));
        }
    }

    @Override
    public void stopScan() {
        if (btAdapter.isEnabled()) {
            AsyncTask.execute(() -> btScanner.stopScan(leScanCallback));
        }
    }

    @Override
    public BluetoothGattCharacteristic getCharac() {
        return charac;
    }

    @Override
    public BluetoothGatt getGatt() {
        return bluetoothGatt;
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
            Log.i(TAG, "device read or wrote to \n");
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case 0:
                        Log.i(TAG, "device disconnected\n");
                        btConnectFragment.connectedDevice = null;
                        btConnectFragment.connected = false;
                        btConnectFragment.deviceList.removeAllViews();
                        mainActivity.navigateToConnectFragment();
                    break;
                case 2:
                        Log.i(TAG, "device connected\n");
                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();
                    break;
                default:
                    Log.i(TAG, "we encounterned an unknown state, uh oh\n");
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

        Log.i(TAG, characteristic.getUuid().toString());
    }

    public boolean connectToDeviceSelected(BluetoothDevice device) {
        bluetoothGatt = device.connectGatt(mainActivity.getApplicationContext(), false, btleGattCallback);
        return bluetoothGatt != null;
    }

    public void disconnectDeviceSelected() {
        bluetoothGatt.disconnect();
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            final String uuid = gattService.getUuid().toString();
            Log.i(TAG, "Service discovered: " + uuid);
            new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                charac = gattCharacteristic;
                final String charUuid = gattCharacteristic.getUuid().toString();
                Log.i(TAG,"Characteristic discovered for service: " + charUuid);

            }
        }
    }
}
