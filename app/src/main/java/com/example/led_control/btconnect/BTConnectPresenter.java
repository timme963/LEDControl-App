package com.example.led_control.btconnect;

import com.example.led_control.MainActivity;

public class BTConnectPresenter implements BTConnectContract.Presenter{
    private final MainActivity mainActivity;
    private BTConnectFragment btConnectFragment;

    public BTConnectPresenter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void setView(BTConnectFragment btConnectFragment) {
        this.btConnectFragment = btConnectFragment;
    }
}
