package com.tnissinen.robotcontrol;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Main fragment of the application. Draws controls for adjusting servo rotations, buttons for memory slots and tab view for log and route fragments.
 */
public class MainActivityFragment extends Fragment  {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View result = inflater.inflate(R.layout.fragment_main, container, false);
        return result;
    }


}
