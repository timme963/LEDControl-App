package com.example.led_control.effects;

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

public class EffectsPresenter implements EffectsContract.Presenter{
    private final MainActivity mainActivity;
    private EffectsFragment effectsFragment;
    private static final String TAG = "LED-ControlAPP";

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;

    public EffectsPresenter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        btManager = (BluetoothManager) mainActivity.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
    }

    @Override
    public void setView(EffectsFragment effectsFragment) {
        this.effectsFragment = effectsFragment;
    }

    public void write(BluetoothGattCharacteristic charac, String data, BluetoothGatt Gatt) {
        writeCharacteristic(charac, data, Gatt);
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
}
