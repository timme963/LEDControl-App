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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BTConnectPresenter implements BTConnectContract.Presenter {
    private final MainActivity mainActivity;
    private BTConnectFragment btConnectFragment;

    private static final String TAG = "LED-ControlAPP";
    // Stops scanning after 5 seconds.

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;

    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<>();
    ArrayList<BluetoothGatt> bluetoothGatt = new ArrayList<>();
    ArrayList<BluetoothGattCharacteristic> charac = new ArrayList<>();
    int color;
    String bright;
    private boolean connected;

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
            if (btAdapter == null || btScanner == null) {
                btAdapter = btManager.getAdapter();
                btScanner = btAdapter.getBluetoothLeScanner();
            }
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
    public ArrayList<BluetoothGattCharacteristic> getCharac() {
        return charac;
    }

    @Override
    public ArrayList<BluetoothGatt> getGatt() {
        return bluetoothGatt;
    }

    // Device scan callback.
    private final ScanCallback leScanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
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

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            Log.i(TAG, "device read or wrote to \n");
            broadcastUpdate(characteristic);
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case 0:
                    Log.i(TAG, "device disconnected\n");
                    btConnectFragment.connectedDevice.remove(gatt.getDevice());
                    btConnectFragment.connected = false;
                    btConnectFragment.deviceList.removeAllViews();
                    mainActivity.navigateToConnectFragment();
                    break;
                case 2:
                    Log.i(TAG, "device connected\n");
                    // discover services and characteristics for this device
                    gatt.discoverServices();
                    break;
                default:
                    Log.i(TAG, "we encounterned an unknown state, uh oh\n");
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            displayGattServices(gatt.getServices());
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
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

    private int intColor(int color1, int color2, int color3) {
        int Red = (color1 << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        int Green = (color2 << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        int Blue = color3 & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void broadcastUpdate(final BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, characteristic.getUuid().toString());
        String newData = new String(characteristic.getValue(), StandardCharsets.UTF_8);
        if (newData.startsWith("b")) {
            bright = newData.substring(1);
        }
        if (newData.startsWith("c")) {
            int color1 = Integer.parseInt(newData.substring(0,3));
            int color2 = Integer.parseInt(newData.substring(4,7));
            int color3 = Integer.parseInt(newData.substring(8));
            color = intColor(color1, color2, color3);
            //color = Integer.parseInt(newData.substring(1));
        }
        if (mainActivity.getHomeFragment() != null) {
            if (newData.startsWith("b")) {
                mainActivity.getHomeFragment().setBrightness(newData.substring(1).equals("255"));
            }
            if (newData.startsWith("c")){
                mainActivity.getHomeFragment().setColor(color);
            }
        }
        System.out.println(newData);
    }

    public String getBright() {
        return bright;
    }

    public int getColor() {
        return color;
    }

    public boolean connectToDeviceSelected(BluetoothDevice device) {
        bluetoothGatt.add(device.connectGatt(mainActivity.getApplicationContext(),true, btleGattCallback));
        connected = false;
        return true;
    }

    public void disconnectDeviceSelected(BluetoothDevice device) {
        for (int i = 0; i < bluetoothGatt.size(); i++) {
            if (bluetoothGatt.get(i).getDevice().equals(device)) {
                bluetoothGatt.get(i).disconnect();
                bluetoothGatt.remove(bluetoothGatt.get(i));
                charac.remove(charac.get(i));
            }
        }
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

                final String charUuid = gattCharacteristic.getUuid().toString();
                Log.i(TAG,"Characteristic discovered for service: " + charUuid);

            }
        }
        charac.add(gattServices.get(gattServices.size()-1).getCharacteristics().get(
                gattServices.get(gattServices.size()-1).getCharacteristics().size() -1));
        bluetoothGatt.get(bluetoothGatt.size()-1).setCharacteristicNotification(charac.get(charac.size()-1), true);
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

    public void sendConnected() {
        if (!connected) {
            writeCharacteristic(charac.get(charac.size()-1), "connected", bluetoothGatt.get(charac.size()-1));
            connected = true;
        }
    }
}
