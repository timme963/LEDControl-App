package com.example.led_control.home;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

public interface HomeContract {

    interface View { // from presenter to view

    }

    interface Model { // from presenter to model and back

    }

    interface Presenter { // from view/service to presenter (and back)
        void setView(HomeFragment homeFragment);

        void write(BluetoothGattCharacteristic charac, String on, BluetoothGatt bluetoothGatt);
    }
}
