package com.example.led_control;

import com.example.led_control.home.HomeFragment;

public class MainPresenter implements MainContract.Presenter {
    private final MainActivity mainActivity;
    private HomeFragment homeFragment;

    public MainPresenter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void setView(HomeFragment homeFragment) {
        this.homeFragment = homeFragment;
    }

    @Override
    public void navigateToHomeFragment() {
        mainActivity.navigateToHomeFragment();
    }

    public MainActivity getActivity() {
      return mainActivity;
    }
}
