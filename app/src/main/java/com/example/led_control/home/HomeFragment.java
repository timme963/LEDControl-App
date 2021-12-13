package com.example.led_control.home;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.led_control.MainPresenter;
import com.example.led_control.R;
import com.example.led_control.btconnect.BTConnectPresenter;

import java.text.NumberFormat;

import top.defaults.colorpicker.ColorPickerView;

public class HomeFragment extends Fragment implements HomeContract.View {
    private final MainPresenter mainPresenter;
    private HomePresenter homePresenter;
    private BTConnectPresenter btConnectPresenter;
    private ImageButton SettingsButton;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private ToggleButton OnOffBtn;
    private Button effect;
    private BluetoothGattCharacteristic charac;
    private BluetoothGatt bluetoothGatt;
    NumberFormat nf;

    public HomeFragment(MainPresenter mainPresenter, HomePresenter homePresenter, BTConnectPresenter btConnectPresenter) {
        this.mainPresenter = mainPresenter;
        this.homePresenter = homePresenter;
        this.btConnectPresenter = btConnectPresenter;

        homePresenter.setView(this);
    }

    /**
     * First method where FragmentView is creating
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    /**
     * Second method where the view is ready to use
     */
    @SuppressLint({"UseSwitchCompatOrMaterialCode", "ClickableViewAccessibility"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SettingsButton = view.findViewById(R.id.SettingsButton);
        OnOffBtn = view.findViewById(R.id.onOff);
        effect = view.findViewById(R.id.effects);
        OnOffBtn.setActivated(true);
        OnOffBtn.setChecked(true);
        homePresenter.write(charac, "on", bluetoothGatt);

        charac = btConnectPresenter.getCharac();
        bluetoothGatt = btConnectPresenter.getGatt();

        ColorPickerView colorPicker = view.findViewById(R.id.colorPicker);
        nf = NumberFormat.getIntegerInstance();
        nf.setMinimumIntegerDigits(3);
        nf.setGroupingUsed(false);

        colorPicker.setInitialColor(0x7F313C93);

        colorPicker.subscribe((color, fromUser, shouldPropagate) -> {
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            homePresenter.write(charac, "c" + nf.format(r) + " " + nf.format(g) + " " + nf.format(b), bluetoothGatt);
            });

        setupOnListener();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupOnListener() {
        SettingsButton.setOnClickListener(v -> mainPresenter.navigateToSettingsFragment());
        OnOffBtn.setOnClickListener(v -> {
            if (!OnOffBtn.isActivated()) {
                homePresenter.write(charac, "on", bluetoothGatt);
                OnOffBtn.setActivated(true);
            } else {
                homePresenter.write(charac, "off", bluetoothGatt);
                OnOffBtn.setActivated(false);
            }
        });
        effect.setOnClickListener(v -> {
            mainPresenter.navigateToEffectsFragment();
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
