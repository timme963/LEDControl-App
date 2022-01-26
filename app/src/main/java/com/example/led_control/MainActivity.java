package com.example.led_control;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.led_control.btconnect.BTConnectFragment;
import com.example.led_control.btconnect.BTConnectPresenter;
import com.example.led_control.effects.EffectsFragment;
import com.example.led_control.effects.EffectsPresenter;
import com.example.led_control.home.HomeFragment;
import com.example.led_control.home.HomePresenter;
import com.example.led_control.settings.SettingsFragment;
import com.example.led_control.settings.SettingsPresenter;
import com.example.led_control.title.TitleFragment;
import com.example.led_control.title.TitlePresenter;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    HomeFragment homeFragment;
    BTConnectFragment btConnectFragment;
    SettingsFragment settingsFragment;
    EffectsFragment effectsFragment;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup Presenter
        HomePresenter homePresenter = new HomePresenter(this);
        MainPresenter mainPresenter = new MainPresenter(this);
        BTConnectPresenter btConnectPresenter = new BTConnectPresenter(this);
        EffectsPresenter effectsPresenter = new EffectsPresenter(this);
        SettingsPresenter settingsPresenter = new SettingsPresenter(this);
        TitlePresenter titlePresenter = new TitlePresenter(this);

        //Setup Fragments
        homeFragment = new HomeFragment(mainPresenter, homePresenter, btConnectPresenter, settingsPresenter);
        btConnectFragment = new BTConnectFragment(mainPresenter, btConnectPresenter);
        settingsFragment = new SettingsFragment(mainPresenter, settingsPresenter, btConnectPresenter);
        effectsFragment = new EffectsFragment(mainPresenter, effectsPresenter, btConnectPresenter, settingsPresenter);
        TitleFragment titleFragment = new TitleFragment(mainPresenter, titlePresenter);

        // set homeFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    titleFragment).commit();
        }

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(dialog -> requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION));
            builder.show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    // checkt ob alle Berechtigungen gegeben sind
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ActivityCompat.requestPermissions(
                this,
                new String[]
                        {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        }, 0);

        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("coarse location permission granted");
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Functionality limited");
                builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(dialog -> {
                });
                builder.show();
            }
        }
    }

    @Override
    public void navigateToHomeFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
    }

    @Override
    public void navigateToConnectFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, btConnectFragment)
                .commit();
    }

    @Override
    public void navigateToEffectFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, effectsFragment)
                .commit();
    }

    @Override
    public void navigateToSettingsFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, settingsFragment)
                .commit();
    }

    public HomeFragment getHomeFragment() {
        return homeFragment;
    }
}