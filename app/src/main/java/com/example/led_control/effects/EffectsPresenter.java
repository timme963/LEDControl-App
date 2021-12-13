package com.example.led_control.effects;

import com.example.led_control.MainActivity;

public class EffectsPresenter implements EffectsContract.Presenter{
    private final MainActivity mainActivity;
    private EffectsFragment effectsFragment;

    public EffectsPresenter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void setView(EffectsFragment effectsFragment) {
        this.effectsFragment = effectsFragment;
    }
}
