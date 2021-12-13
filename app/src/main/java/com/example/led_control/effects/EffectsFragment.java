package com.example.led_control.effects;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.AsyncTask;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.led_control.MainPresenter;
import com.example.led_control.R;
import com.example.led_control.btconnect.BTConnectPresenter;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Calendar;

public class EffectsFragment extends Fragment implements EffectsContract.View {
    private final MainPresenter mainPresenter;
    private final EffectsPresenter effectsPresenter;
    private BluetoothGattCharacteristic charac;
    private BluetoothGatt bluetoothGatt;
    private BTConnectPresenter btConnectPresenter;
    private ImageButton settingsBtn;
    private Button wakeUp;
    private Button goSleep;
    private Button blink;
    private Button colorChange;
    private Calendar kalender;
    private SimpleDateFormat zeitformat;
    private int intervall;
    private String signal;
    private String time;

    public EffectsFragment(MainPresenter mainPresenter, EffectsPresenter effectsPresenter, BTConnectPresenter btConnectPresenter) {
        this.mainPresenter = mainPresenter;
        this.effectsPresenter = effectsPresenter;
        this.btConnectPresenter = btConnectPresenter;

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
    @SuppressLint({"UseSwitchCompatOrMaterialCode", "SimpleDateFormat"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        charac = btConnectPresenter.getCharac();
        bluetoothGatt = btConnectPresenter.getGatt();
        kalender = Calendar.getInstance();
        zeitformat = new SimpleDateFormat("HH:mm");

        settingsBtn = view.findViewById(R.id.settingsBtn);
        wakeUp = view.findViewById(R.id.wakeUp);
        goSleep = view.findViewById(R.id.goSleep);
        colorChange = view.findViewById(R.id.colorChange);
        blink = view.findViewById(R.id.blink);

        setupOnListener();
    }

    private void setupOnListener() {
        settingsBtn.setOnClickListener(v -> mainPresenter.navigateToSettingsFragment());
        colorChange.setOnClickListener(v -> effectsPresenter.write(charac, "colorChange", bluetoothGatt));//TODO Farben?!
        blink.setOnClickListener(v -> effectsPresenter.write(charac, "blink", bluetoothGatt));//intervall
        goSleep.setOnClickListener(v -> effectsPresenter.write(charac, "gosleep" + "200", bluetoothGatt));//intervall + (time)
        wakeUp.setOnClickListener(v -> {
            time = zeitformat.format(kalender.getTime());
            generateTimePopUp("wakeup");
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String generateIntervallPopUp(String effect) {
        final EditText input = new EditText(getActivity());

        new AlertDialog.Builder(requireActivity())
                .setTitle("Intervall in ms")
                .setView(input)
                .setPositiveButton("Ok", (dialog, whichButton) -> {
                    Editable editable = input.getText();
                    try {
                        if (signal.equals(time)) {
                            long millisBetween = Duration.between(LocalTime.parse(time),
                                    LocalTime.parse(signal)).toMillis();
                            System.out.println(millisBetween + "############################1");
                            intervall = Integer.parseInt(String.valueOf(editable));
                            effectsPresenter.write(charac, effect + intervall, bluetoothGatt);
                        } else {
                            long millisBetween = Duration.between(LocalTime.parse(time),
                                    LocalTime.parse(signal)).toMillis();
                            System.out.println(millisBetween + "############################2");
                            if (millisBetween > 0) {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        intervall = Integer.parseInt(String.valueOf(editable));
                                        effectsPresenter.write(charac, effect + intervall, bluetoothGatt);
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
        return String.valueOf(intervall);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String generateTimePopUp(String effect) {
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
        return signal;
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}

