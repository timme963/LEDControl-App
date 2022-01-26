package com.example.led_control.btconnect;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.led_control.MainPresenter;
import com.example.led_control.R;

import java.util.ArrayList;

public class BTConnectFragment extends Fragment implements BTConnectContract.View {
    private final MainPresenter mainPresenter;
    private final BTConnectPresenter btConnectPresenter;
    private static final long SCAN_PERIOD = 5000;
    private final Handler handler = new Handler();

    Button scan;
    Button stopScan;
    ImageButton btButton;
    LinearLayout deviceList;
    ArrayList<BluetoothDevice> connectedDevice = new ArrayList<>();
    boolean btScan;
    boolean connected = false;

    public BTConnectFragment(MainPresenter mainPresenter, BTConnectPresenter btConnectPresenter) {
        this.mainPresenter = mainPresenter;
        this.btConnectPresenter = btConnectPresenter;

        btConnectPresenter.setView(this);
    }

    /**
     * First method where FragmentView is creating
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_btconnect, container, false);
    }

    /**
     * Second method where the view is ready to use
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btButton = view.findViewById(R.id.BTButton);
        scan = view.findViewById(R.id.scanButton);
        stopScan = view.findViewById(R.id.stopScan);
        stopScan.setVisibility(View.INVISIBLE);
        deviceList = view.findViewById(R.id.BTDeviceList);

        if (btConnectPresenter.btAdapter.isEnabled()) {
            btButton.setBackgroundColor(0xFFBB86FC);
        } else {
            btButton.setBackgroundColor(0xFFD0D3D3);
        }
        // show connected devices
        if (connectedDevice != null) {
            for (BluetoothDevice i : connectedDevice) {
                showDevice(i);
            }
        }

        setupOnListener();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void setupOnListener() {
        // scan for ble devices and show them in a list
        scan.setOnClickListener(v -> {
            deviceList.removeAllViews();
            if (connectedDevice != null) {
                for (BluetoothDevice i : connectedDevice) {
                    showDevice(i);
                }
            }
            btScan = true;
            scan.setVisibility(View.INVISIBLE);
            stopScan.setVisibility(View.VISIBLE);
            btConnectPresenter.startScan();

            handler.postDelayed(() -> {
                scan.setVisibility(View.VISIBLE);
                stopScan.setVisibility(View.INVISIBLE);
                btConnectPresenter.stopScan();
                btScan = false;

            }, SCAN_PERIOD);
        });

        stopScan.setOnClickListener(v -> {
            scan.setVisibility(View.VISIBLE);
            stopScan.setVisibility(View.INVISIBLE);
            btConnectPresenter.stopScan();
            btScan = false;
        });

        // set btbutton
        btButton.setOnClickListener(v -> {
            if (!btConnectPresenter.btAdapter.isEnabled()) {
                btConnectPresenter.btAdapter.enable();
                btButton.setBackgroundColor(0xFFBB86FC);
            } else {
                btConnectPresenter.btAdapter.disable();
                btButton.setBackgroundColor(0xFFD0D3D3);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("SetTextI18n")
    @Override
    // list with daveice name and connect/disconnect button
    public void showDevice(BluetoothDevice device) {
        // set name and button
        LinearLayout layout = new LinearLayout(getActivity());
        Button btn = new Button(getActivity());
        if (connectedDevice.contains(device)) {
            btn.setText("Disconnect");
        } else {
            btn.setText("Connect");
        }
        TextView txt = new TextView(getActivity());
        txt.setText(device.getName());
        txt.setPadding(20,0,50,0);
        txt.setMaxWidth(550);
        txt.setMinWidth(550);
        layout.addView(txt);
        btn.setMinWidth(400);
        layout.addView(btn);
        deviceList.addView(layout);
        // set button click listener
        btn.setOnClickListener(v -> {
            if (btn.getText() == "Connect") {
                connected = btConnectPresenter.connectToDeviceSelected(device);
                if (connected) {
                    connectedDevice.add(device);
                    btn.setText("Disconnect");
                    handler.postDelayed(mainPresenter::navigateToSettingsFragment, 3000);
                }
            } else {
                btConnectPresenter.disconnectDeviceSelected(device);
                btn.setText("Connect");
                connectedDevice.remove(device);
                connected = false;
            }
        });
        txt.setOnClickListener(v -> {
            if (connectedDevice.contains(device)) {
                handler.postDelayed(mainPresenter::navigateToSettingsFragment, 1000);
            }
        });
    }
}
