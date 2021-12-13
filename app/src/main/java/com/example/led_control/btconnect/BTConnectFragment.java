package com.example.led_control.btconnect;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
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
import androidx.fragment.app.Fragment;

import com.example.led_control.MainPresenter;
import com.example.led_control.R;

public class BTConnectFragment extends Fragment implements BTConnectContract.View {
    private final MainPresenter mainPresenter;
    private final BTConnectPresenter btConnectPresenter;
    private static final long SCAN_PERIOD = 5000;
    private final Handler handler = new Handler();

    Button scan;
    Button stopScan;
    ImageButton btButton;
    LinearLayout deviceList;
    BluetoothDevice connectedDevice;
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
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btButton = view.findViewById(R.id.BTButton);
        scan = view.findViewById(R.id.scanButton);
        stopScan = view.findViewById(R.id.stopScan);
        stopScan.setVisibility(View.INVISIBLE);
        deviceList = view.findViewById(R.id.BTDeviceList);

        if (btConnectPresenter.btAdapter.isEnabled()) {
            btButton.setBackgroundColor((int) 0xFF1C9191);
        } else {
            btButton.setBackgroundColor((int) 0xFFD0D3D3);
        }
        if (connectedDevice != null) {
            showDevice(connectedDevice);
        }

        setupOnListener();
    }

    private void setupOnListener() {
        scan.setOnClickListener(v -> {
            deviceList.removeAllViews();
            if (connectedDevice != null) {
                showDevice(connectedDevice);
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

        btButton.setOnClickListener(v -> {
            if (!btConnectPresenter.btAdapter.isEnabled()) {
                btConnectPresenter.btAdapter.enable();
                btButton.setBackgroundColor((int) 0xFF1C9191);
            } else {
                btConnectPresenter.btAdapter.disable();
                btButton.setBackgroundColor((int) 0xFFD0D3D3);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void showDevice(BluetoothDevice device) {
        LinearLayout layout = new LinearLayout(getActivity());
        Button btn = new Button(getActivity());
        if (device == connectedDevice) {
            btn.setText("Disconnect");
        } else {
            btn.setText("Connect");
        }
        TextView txt = new TextView(getActivity());
        txt.setText(device.getName());
        txt.setPadding(10,0,50,0);
        layout.addView(txt);
        layout.addView(btn);
        deviceList.addView(layout);
        btn.setOnClickListener(v -> {
            if (btn.getText() == "Connect") {
                connectedDevice = device;
                connected = btConnectPresenter.connectToDeviceSelected(device);
                btn.setText("Disconnect");
                if (connected) {
                    handler.postDelayed(mainPresenter::navigateToHomeFragment, 3000);
                }
            } else {
                btConnectPresenter.disconnectDeviceSelected();
                btn.setText("Connect");
                connectedDevice = null;
                connected = false;
            }
        });
        txt.setOnClickListener(v -> {
            if (connected) {
                handler.postDelayed(mainPresenter::navigateToHomeFragment, 1000);
            }
        });
    }

}
