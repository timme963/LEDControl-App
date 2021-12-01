package com.example.led_control.home;

import com.example.led_control.MainActivity;

public class HomePresenter implements HomeContract.Presenter {

    private final MainActivity mainActivity;
    private HomeFragment homeFragment;

    public HomePresenter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void setView(HomeFragment homeFragment) {
        this.homeFragment = homeFragment;
        this.homeFragment.setText("String");
    }
}
