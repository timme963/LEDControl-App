package com.example.led_control.effects;

public interface EffectsContract {
    interface View {// from presenter to view
    }

    interface Model { // from presenter to model and back

    }

    interface Presenter {
        void setView(EffectsFragment effectsFragment); // from view/service to presenter (and back)
    }
}
