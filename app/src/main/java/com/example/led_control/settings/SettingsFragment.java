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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.led_control.MainPresenter;
import com.example.led_control.R;
import com.example.led_control.btconnect.BTConnectPresenter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsFragment extends Fragment implements SettingsContract.View {
    private final MainPresenter mainPresenter;
    private final SettingsPresenter settingsPresenter;
    private BTConnectPresenter btConnectPresenter;
    private BluetoothGattCharacteristic charac;
    private BluetoothGatt bluetoothGatt;
    private ListView settingList;
    private int anzahl;

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
                "Anzahl LEDs",
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

        setupOnListener();
    }

    private void setupOnListener() {
    }

    private AdapterView.OnItemClickListener listClick = new AdapterView.OnItemClickListener() {
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
                                settingsPresenter.write(charac, "num" + anzahl, bluetoothGatt);
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

    @Override
    public void onStop() {
        super.onStop();
    }
}

