package com.example.led_control.btconnect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.led_control.MainPresenter;
import com.example.led_control.R;

public class BTConnectFragment extends Fragment implements BTConnectContract.View{
    private View view;
    private MainPresenter mainPresenter;
    private BTConnectPresenter btConnectPresenter;

    public BTConnectFragment(MainPresenter mainPresenter, BTConnectPresenter btConnectPresenter) {
        this.mainPresenter = mainPresenter;
        this.btConnectPresenter = btConnectPresenter;

        btConnectPresenter.setView(this);
    }

    /**
     * First method where FragmentView is creating
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_btconnect, container, false);
        return view;
    }

    /**
     * Second method where the view is ready to use
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupOnListener();
    }

    private void setupOnListener() {

    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
