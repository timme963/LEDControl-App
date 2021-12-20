package com.example.led_control.effects;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.led_control.MainPresenter;
import com.example.led_control.R;
import com.example.led_control.btconnect.BTConnectPresenter;
import com.example.led_control.settings.SettingsPresenter;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;

public class EffectsFragment extends Fragment implements EffectsContract.View {
    private final MainPresenter mainPresenter;
    private final EffectsPresenter effectsPresenter;
    private final SettingsPresenter settingsPresenter;
    private ArrayList<BluetoothGattCharacteristic> charac = new ArrayList<>();
    private ArrayList<BluetoothGatt> bluetoothGatt;
    private BTConnectPresenter btConnectPresenter;
    private ImageView settingsBtn;
    private Button wakeUp;
    private Button goSleep;
    private Button blink;
    private Button colorChange;
    private Calendar kalender;
    private SimpleDateFormat zeitformat;
    private int intervall;
    private String signal;
    private String time;
    private Button fire;
    private Button cylon;

    public EffectsFragment(MainPresenter mainPresenter, EffectsPresenter effectsPresenter, BTConnectPresenter btConnectPresenter, SettingsPresenter settingsPresenter) {
        this.mainPresenter = mainPresenter;
        this.effectsPresenter = effectsPresenter;
        this.btConnectPresenter = btConnectPresenter;
        this.settingsPresenter = settingsPresenter;

        effectsPresenter.setView(this);
    }

    /**
     * First method where FragmentView is creating
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_effects, container, false);
    }

    /**
     * Second method where the view is ready to use
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"UseSwitchCompatOrMaterialCode", "SimpleDateFormat"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bluetoothGatt = settingsPresenter.getGatt();
        charac = settingsPresenter.getCharac();
        kalender = Calendar.getInstance();
        zeitformat = new SimpleDateFormat("HH:mm");

        settingsBtn = view.findViewById(R.id.settingsBtn);
        wakeUp = view.findViewById(R.id.wakeUp);
        goSleep = view.findViewById(R.id.goSleep);
        colorChange = view.findViewById(R.id.colorChange);
        blink = view.findViewById(R.id.blink);
        fire = view.findViewById(R.id.fire);
        cylon = view.findViewById(R.id.cylon);

        //TODO mehr effekte?
        //TODO beacon?
        //TODO sound reaction?
        //TODO background service

        setupOnListener();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupOnListener() {
        settingsBtn.setOnClickListener(v -> mainPresenter.navigateToSettingsFragment());
        colorChange.setOnClickListener(v -> {
                    for(int i = 0; i < bluetoothGatt.size(); i++) {
                        effectsPresenter.write(charac.get(i), "eColor" + "1000", bluetoothGatt.get(i));
                    }
        });//TODO Farben und variables intervall?!
        blink.setOnClickListener(v -> {
            time = zeitformat.format(kalender.getTime());
            signal = time;
            generateIntervallPopUp("eblink");
        });
        goSleep.setOnClickListener(v -> {
            time = zeitformat.format(kalender.getTime());
            generateTimePopUp("gosleep");
        });
        wakeUp.setOnClickListener(v -> {
            time = zeitformat.format(kalender.getTime());
            generateTimePopUp("wakeup");
        });
        cylon.setOnClickListener(v -> {
            for(int i = 0; i < bluetoothGatt.size(); i++) {
                effectsPresenter.write(charac.get(i), "ecylon", bluetoothGatt.get(i));
            }
        });
        fire.setOnClickListener(v -> {
            for(int i = 0; i < bluetoothGatt.size(); i++) {
                effectsPresenter.write(charac.get(i), "efiree", bluetoothGatt.get(i));
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generateIntervallPopUp(String effect) {
        final EditText input = new EditText(getActivity());

        new AlertDialog.Builder(requireActivity())
                .setTitle("Intervall in ms")
                .setView(input)
                .setPositiveButton("Ok", (dialog, whichButton) -> {
                    Editable editable = input.getText();
                    try {
                        long millisBetween = Duration.between(LocalTime.parse(time),
                                LocalTime.parse(signal)).toMillis();
                        if (signal.equals(time)) {
                            intervall = Integer.parseInt(String.valueOf(editable));
                            for(int i = 0; i < bluetoothGatt.size(); i++) {
                                effectsPresenter.write(charac.get(i), effect + intervall, bluetoothGatt.get(i));
                            }
                        } else {
                            if (millisBetween > 0) {
                                Handler handler = new Handler();
                                handler.postDelayed(() -> {
                                    intervall = Integer.parseInt(String.valueOf(editable));
                                    for(int i = 0; i < bluetoothGatt.size(); i++) {
                                        effectsPresenter.write(charac.get(i), effect + intervall, bluetoothGatt.get(i));
                                    }
                                }, millisBetween);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton("Cancel", (dialog, whichButton) -> {
                    // Do nothing.
                }).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generateTimePopUp(String effect) {
        final EditText input = new EditText(getActivity());

        new AlertDialog.Builder(requireActivity())
                .setTitle("Zeitpunkt in hh:mm")
                .setView(input)
                .setPositiveButton("Ok", (dialog, whichButton) -> {
                    Editable editable = input.getText();
                    signal = editable.toString();
                    generateIntervallPopUp(effect);
                })
                .setNegativeButton("Cancel", (dialog, whichButton) -> {
                    // Do nothing.
                }).show();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}

