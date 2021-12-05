package com.example.led_control.home;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.led_control.MainPresenter;
import com.example.led_control.R;
import com.example.led_control.btconnect.BTConnectPresenter;

public class HomeFragment extends Fragment implements HomeContract.View {
    private final MainPresenter mainPresenter;
    private final HomePresenter homePresenter;
    private BTConnectPresenter btConnectPresenter;
    private ImageButton BTButton;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch OnOffBtn;
    private Button effect;
    private SeekBar brightness;
    private BluetoothGattCharacteristic charac;
    BluetoothGatt bluetoothGatt;

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
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BTButton = view.findViewById(R.id.BTButton2);
        OnOffBtn = view.findViewById(R.id.onOff);
        effect = view.findViewById(R.id.effects);
        brightness = (SeekBar) view.findViewById(R.id.Brightness);
        OnOffBtn.setActivated(false);

        charac = btConnectPresenter.getCharac();
        bluetoothGatt = btConnectPresenter.getGatt();

        setupOnListener();
    }

    private void setupOnListener() {
        //TODO ändern in einstellungen
        BTButton.setOnClickListener(v -> mainPresenter.navigateToConnectFragment());
        OnOffBtn.setOnClickListener(v -> {
            if (!OnOffBtn.isActivated()) {
                homePresenter.write(charac, "on", bluetoothGatt);
                OnOffBtn.setActivated(true);
            } else {
                homePresenter.write(charac, "off", bluetoothGatt);
                OnOffBtn.setActivated(false);
            }
        });
        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int newProgress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                newProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                homePresenter.write(charac, "bright" + newProgress, bluetoothGatt);
            }
        });
        effect.setOnClickListener(v -> {
            //TODO effekte fragment
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
