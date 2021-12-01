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
    private View view;
    private MainPresenter mainPresenter;
    private BTConnectPresenter btConnectPresenter;
    private static final long SCAN_PERIOD = 5000;
    private final Handler handler = new Handler();

    Button scan;
    Button stopScan;
    ImageButton btButton;
    LinearLayout deviceList;

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
        view = inflater.inflate(R.layout.fragment_btconnect, container, false);
        return view;
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

        setupOnListener();
    }

    private void setupOnListener() {
        scan.setOnClickListener(v -> {
            deviceList.removeAllViews();
            //textView.setText("");
            //Log.i(TAG, "Suche nach BLE Geräte > beginn");
            //textView.append("Started Scanning\n");
            scan.setVisibility(View.INVISIBLE);
            stopScan.setVisibility(View.VISIBLE);
            btConnectPresenter.startScan();


            handler.postDelayed(() -> {
                //Log.i(TAG, "Suche nach BLE Geräte > ende");
                //textView.append("Stop Scanning\n");
                scan.setVisibility(View.VISIBLE);
                stopScan.setVisibility(View.INVISIBLE);
                btConnectPresenter.stopScan();

            }, SCAN_PERIOD);
        });

        stopScan.setOnClickListener(v -> {
            //System.out.println("stopping scanning");
            //textView.append("Stopped Scanning\n");
            scan.setVisibility(View.VISIBLE);
            stopScan.setVisibility(View.INVISIBLE);
            btConnectPresenter.stopScan();
        });

        btButton.setOnClickListener(v -> {

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
        btn.setText("Connect");
        TextView txt = new TextView(getActivity());
        txt.setText("Device Name: " + device.getName());
        layout.addView(txt);
        layout.addView(btn);
        deviceList.addView(layout);
        btn.setOnClickListener(v -> {
            if (btn.getText() == "Connect") {
                btConnectPresenter.connectToDeviceSelected(device);
                btn.setText("Disconnect");
            } else {
                btConnectPresenter.disconnectDeviceSelected();
                btn.setText("Connect");
            }
        });
    }
}
