package com.tnissinen.robotcontrol;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ListView;

/**
 * Fragment for listing bonded bluetooth devices
 */
public class DevicesActivityFragment extends Fragment {

    public DevicesActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_devices, container, false);
    }
}
