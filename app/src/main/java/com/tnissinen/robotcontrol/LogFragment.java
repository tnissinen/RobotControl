package com.tnissinen.robotcontrol;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Fragment for showing text log of actions in the application. Mainly bluetooth communication.
 */
public class LogFragment extends Fragment {

    private TextView logTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result =  inflater.inflate(R.layout.fragment_log, container, false);

        logTextView = result.findViewById(R.id.logTextView);

        logTextView.setMovementMethod(new ScrollingMovementMethod());
        registerForContextMenu(logTextView);

        return result;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(getString(R.string.clear_log));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getItemId() == 0)
            logTextView.setText("");

        return true;
    }

    /**
     * Adds a line to log text view
     * @param line line to add
     */
    public void addLine(String line){

        Date currentTime = Calendar.getInstance().getTime();
        String currentTimeString = new SimpleDateFormat("HH:mm:ss").format(currentTime);

        String newLineText = "";
        if(logTextView.getText().length() > 0)
            newLineText += logTextView.getText() + "\n";

        newLineText += currentTimeString + " - " + line;

        logTextView.setText(newLineText);
    }

}
