package com.example.led_control.settings;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.util.Log;

import com.example.led_control.MainActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SettingsPresenter implements SettingsContract.Presenter{
    private final MainActivity mainActivity;
    private SettingsFragment settingsFragment;
    private static final String TAG = "LED-ControlAPP";

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;

    public SettingsPresenter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        btManager = (BluetoothManager) mainActivity.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
    }

    @Override
    public void setView(SettingsFragment settingsFragment) {
        this.settingsFragment = settingsFragment;
    }

    public Runnable write(BluetoothGattCharacteristic charac, String data, BluetoothGatt Gatt) {
        writeCharacteristic(charac, data, Gatt);
        return null;
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic,
                                    String data, BluetoothGatt bluetoothGatt) {
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

    public ArrayList<BluetoothGatt> getGatt() {
        return settingsFragment.getBluetoothGatt();
    }

    public ArrayList<BluetoothGattCharacteristic> getCharac() {
        return settingsFragment.getCharac();
    }
}
