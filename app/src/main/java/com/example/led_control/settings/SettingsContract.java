package com.example.led_control.settings;

public interface SettingsContract {
    interface View {// from presenter to view
    }

    interface Model { // from presenter to model and back

    }

    interface Presenter {
        void setView(SettingsFragment settingsFragment); // from view/service to presenter (and back)
    }
}
