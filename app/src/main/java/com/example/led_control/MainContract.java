package com.example.led_control;

import com.example.led_control.home.HomeFragment;

public interface MainContract {
    interface View {
        void navigateToHomeFragment(); // from presenter to view

    }

    interface Model { // from presenter to model and back

    }

    interface Presenter { // from view/service to presenter (and back)
        void setView(HomeFragment homeFragment);

        void navigateToHomeFragment();

    }
}
