package com.example.led_control.title;

public interface TitleContract {
    interface View {// from presenter to view
    }

    interface Model { // from presenter to model and back

    }

    interface Presenter {
        void setView(TitleFragment titleFragment); // from view/service to presenter (and back)
    }
}
