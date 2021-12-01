package com.example.led_control.home;

public interface HomeContract {

    interface View { // from presenter to view
        void setText(String text);

    }

    interface Model { // from presenter to model and back

    }

    interface Presenter { // from view/service to presenter (and back)
        void setView(HomeFragment homeFragment);
    }
}
