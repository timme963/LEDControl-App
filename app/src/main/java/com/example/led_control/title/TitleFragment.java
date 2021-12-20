package com.example.led_control.title;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.led_control.MainPresenter;
import com.example.led_control.R;

public class TitleFragment extends Fragment implements TitleContract.View {
    private final MainPresenter mainPresenter;
    private final TitlePresenter titlePresenter;

    public TitleFragment(MainPresenter mainPresenter, TitlePresenter titlePresenter) {
        this.mainPresenter = mainPresenter;
        this.titlePresenter = titlePresenter;

        titlePresenter.setView(this);
    }

    /**
     * First method where FragmentView is creating
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.title_page, container, false);
    }

    /**
     * Second method where the view is ready to use
     */
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupOnListener();
        startApp();
    }

    public void startApp() {
        Handler handler = new Handler();
        // Actions to do after 10 seconds
        handler.postDelayed(mainPresenter::navigateToConnectFragment, 3000);

    }

    private void setupOnListener() {
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
