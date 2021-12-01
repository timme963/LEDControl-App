package com.example.led_control.btconnect;

import android.bluetooth.BluetoothDevice;

public interface BTConnectContract {
    interface View {
        void showDevice(BluetoothDevice device); // from presenter to view

    }

    interface Model { // from presenter to model and back

    }

    interface Presenter { // from view/service to presenter (and back)
        void setView(BTConnectFragment btConnectFragment);

        void startScan();

        void stopScan();
    }
}
