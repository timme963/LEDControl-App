package com.example.led_control.effects;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.led_control.MainPresenter;
import com.example.led_control.R;

public class EffectsFragment extends Fragment implements EffectsContract.View {
    private final MainPresenter mainPresenter;
    private final EffectsPresenter effectsPresenter;
    private View settingsBtn;

    public EffectsFragment(MainPresenter mainPresenter, EffectsPresenter effectsPresenter) {
        this.mainPresenter = mainPresenter;
        this.effectsPresenter = effectsPresenter;

        effectsPresenter.setView(this);
    }

    /**
     * First method where FragmentView is creating
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_effects, container, false);
    }

    /**
     * Second method where the view is ready to use
     */
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingsBtn = view.findViewById(R.id.settingsBtn);

        setupOnListener();
    }

    private void setupOnListener() {
        settingsBtn.setOnClickListener(v -> mainPresenter.navigateToSettingsFragment());
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}

