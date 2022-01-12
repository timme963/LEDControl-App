package com.example.led_control.home;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.led_control.MainPresenter;
import com.example.led_control.R;
import com.example.led_control.btconnect.BTConnectPresenter;
import com.example.led_control.settings.SettingsPresenter;

import java.text.NumberFormat;
import java.util.ArrayList;

import top.defaults.colorpicker.ColorPickerView;

public class HomeFragment extends Fragment implements HomeContract.View {
    private final MainPresenter mainPresenter;
    private final SettingsPresenter settingsPresenter;
    private HomePresenter homePresenter;
    private BTConnectPresenter btConnectPresenter;
    private ImageView SettingsButton;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private ToggleButton OnOffBtn;
    private Button effect;
    private ColorPickerView colorPicker;
    private ArrayList<BluetoothGattCharacteristic> charac = new ArrayList<>();
    private ArrayList<BluetoothGatt> bluetoothGatt = new ArrayList<>();
    private NumberFormat nf;
    private int colour;

    public HomeFragment(MainPresenter mainPresenter, HomePresenter homePresenter, BTConnectPresenter btConnectPresenter, SettingsPresenter settingsPresenter) {
        this.mainPresenter = mainPresenter;
        this.homePresenter = homePresenter;
        this.btConnectPresenter = btConnectPresenter;
        this.settingsPresenter = settingsPresenter;

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
        OnOffBtn.setEnabled(true);

        if (btConnectPresenter.getBright() != null) {
            if (btConnectPresenter.getBright().equals("0")) {
                OnOffBtn.setActivated(false);
            }
        } else {
            for (int i = 0; i < bluetoothGatt.size(); i++) {
                homePresenter.write(charac.get(i), "on", bluetoothGatt.get(i));
            }
        }

        charac = settingsPresenter.getCharac();
        bluetoothGatt = settingsPresenter.getGatt();

        colorPicker = view.findViewById(R.id.colorPicker);
        nf = NumberFormat.getIntegerInstance();
        nf.setMinimumIntegerDigits(3);
        nf.setGroupingUsed(false);

        //colorPicker.setInitialColor(0x7F313C93); // if you want to set a color when you open home

        //load last color
        /*final int[] colour = new int[1];
        if (btConnectPresenter.getColor() != 0) {
            colour[0] = (btConnectPresenter.getColor());
            colorPicker.setInitialColor(colour[0]);
        } else {
            colorPicker.setInitialColor(0x00000000);
        }*/
        if (colour != 0) {
            colorPicker.setInitialColor(colour);
        }

        colorPicker.subscribe((color, fromUser, shouldPropagate) -> {
            //if (color != colour[0]) {
                //colour[0] = color;
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                for (int i = 0; i < bluetoothGatt.size(); i++) {
                    homePresenter.write(charac.get(i), "c" + nf.format(r) + " " + nf.format(g) + " " + nf.format(b), bluetoothGatt.get(i));
                }
                colour = color;
            //}
        });

        setupOnListener();
    }

    public void setBrightness(boolean on) {
        OnOffBtn.setActivated(on);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setColor(int color) {
        colorPicker.setInitialColor(color);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupOnListener() {
        SettingsButton.setOnClickListener(v -> mainPresenter.navigateToSettingsFragment());
        OnOffBtn.setOnClickListener(v -> {
            if (!OnOffBtn.isActivated()) {
                for(int i = 0; i < bluetoothGatt.size(); i++) {
                    homePresenter.write(charac.get(i), "on", bluetoothGatt.get(i));
                }
                OnOffBtn.setActivated(true);
            } else {
                for(int i = 0; i < bluetoothGatt.size(); i++) {
                    homePresenter.write(charac.get(i), "off", bluetoothGatt.get(i));
                }
                OnOffBtn.setActivated(false);
            }
        });
        effect.setOnClickListener(v -> mainPresenter.navigateToEffectsFragment());
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
