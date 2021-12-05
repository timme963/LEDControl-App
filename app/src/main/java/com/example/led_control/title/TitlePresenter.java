package com.example.led_control.title;

import com.example.led_control.MainActivity;

public class TitlePresenter implements TitleContract.Presenter{
    private final MainActivity mainActivity;
    private TitleFragment titleFragment;

    public TitlePresenter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void setView(TitleFragment titleFragment) {
        this.titleFragment = titleFragment;
    }
}
