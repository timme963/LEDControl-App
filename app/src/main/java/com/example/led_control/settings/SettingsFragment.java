package com.example.led_control.settings;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.led_control.MainPresenter;
import com.example.led_control.R;
import com.example.led_control.btconnect.BTConnectPresenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsFragment extends Fragment implements SettingsContract.View {
    private final MainPresenter mainPresenter;
    private final SettingsPresenter settingsPresenter;
    private BTConnectPresenter btConnectPresenter;
    private ArrayList<BluetoothGattCharacteristic> charac = new ArrayList<>();
    private ArrayList<BluetoothGatt> bluetoothGatt = new ArrayList<>();
    private ListView settingList;
    private int anzahl;
    private BluetoothGatt currentGatt;
    private BluetoothGattCharacteristic currentCharac;
    private Spinner dropdown;

    public SettingsFragment(MainPresenter mainPresenter, SettingsPresenter settingsPresenter, BTConnectPresenter btConnectPresenter) {
        this.mainPresenter = mainPresenter;
        this.settingsPresenter = settingsPresenter;
        this.btConnectPresenter = btConnectPresenter;

        settingsPresenter.setView(this);
    }

    /**
     * First method where FragmentView is creating
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        String [] listeArray = {
                "Verbindungen",
                "Beleuchtung",
                "Effekte",
                "Anzahl LEDs"
        };

        List<String> settingList = new ArrayList<>(Arrays.asList(listeArray));

        ArrayAdapter<String> listeAdapter =
                new ArrayAdapter<>(
                        getActivity(), // Die aktuelle Umgebung (diese Activity)
                        R.layout.list_item_settingslist, // ID der XML-Layout Datei
                        R.id.list_item_settingsliste_textview, // ID des TextViews
                        settingList); // Beispieldaten in einer ArrayList

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        ListView settingslisteListView = rootView.findViewById(R.id.settingList);
        settingslisteListView.setAdapter(listeAdapter);
        settingslisteListView.setOnItemClickListener(listClick);

        return rootView;
    }

    /**
     * Second method where the view is ready to use
     */
    @SuppressLint({"UseSwitchCompatOrMaterialCode", "SetTextI18n"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        charac = btConnectPresenter.getCharac();
        bluetoothGatt = btConnectPresenter.getGatt();

        settingList = view.findViewById(R.id.settingList);

        dropdown = view.findViewById(R.id.spinner2);
        ArrayList<String> items = new ArrayList<>();
        for (BluetoothGatt i : bluetoothGatt) {
            items.add(i.getDevice().getName());
        }
        items.add("Alle");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),R.layout.list_item_settingslist, R.id.list_item_settingsliste_textview, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(click);

        if (!dropdown.getSelectedItem().equals("Alle")) {
            currentGatt = bluetoothGatt.get(bluetoothGatt.size() -1);
            currentCharac = charac.get(charac.size() -1);
            dropdown.setSelection(bluetoothGatt.size() -1);
        } else {
            dropdown.setSelection(items.size());
        }

        btConnectPresenter.sendConnected();
        setupOnListener();
    }

    private void setupOnListener() {
    }

    private final AdapterView.OnItemSelectedListener click = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position >= bluetoothGatt.size()) {
                currentGatt = null;
                currentCharac = null;
            } else {
                currentGatt = bluetoothGatt.get(position);
                currentCharac = charac.get(position);
            }
            dropdown.setSelection(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //Do nothing
        }
    };

    private final AdapterView.OnItemClickListener listClick = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            String itemValue = settingList.getItemAtPosition(position).toString();
            if (itemValue.equals("Verbindungen")) {
                mainPresenter.navigateToConnectFragment();
            }
            if (itemValue.equals("Beleuchtung")) {
                mainPresenter.navigateToHomeFragment();
            }
            if (itemValue.equals("Effekte")) {
                mainPresenter.navigateToEffectsFragment();
            }
            if (itemValue.equals("Anzahl LEDs")) {

                final EditText input = new EditText(getActivity());

                new AlertDialog.Builder(requireActivity())
                        .setTitle("Update Anz. LEDs")
                        .setView(input)
                        .setPositiveButton("Ok", (dialog, whichButton) -> {
                            Editable editable = input.getText();
                            try {
                                anzahl = Integer.parseInt(String.valueOf(editable));
                                if (currentGatt != null) {
                                    settingsPresenter.write(currentCharac, "num" + anzahl, currentGatt);
                                } else {
                                    for (int i = 0; i < bluetoothGatt.size(); i++) {
                                        settingsPresenter.write(charac.get(i), "num" + anzahl, bluetoothGatt.get(i));
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
        }
    };

    public ArrayList<BluetoothGatt> getBluetoothGatt() {
        if (currentGatt != null) {
            ArrayList<BluetoothGatt> currentGatt = new ArrayList<>();
            currentGatt.add(this.currentGatt);
            return currentGatt;
        } else {
            return bluetoothGatt;
        }
    }

    public ArrayList<BluetoothGattCharacteristic> getCharac() {
        if (currentCharac != null) {
            ArrayList<BluetoothGattCharacteristic> currentCharac = new ArrayList<>();
            currentCharac.add(this.currentCharac);
            return currentCharac;
        } else {
            return charac;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}

