package com.example.led_control.btconnect;

public interface BTConnectContract {
    interface View { // from presenter to view

    }

    interface Model { // from presenter to model and back

    }

    interface Presenter { // from view/service to presenter (and back)
        void setView(BTConnectFragment btConnectFragment);
    }
}
