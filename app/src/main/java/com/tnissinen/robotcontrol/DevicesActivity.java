package com.tnissinen.robotcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Activity for showing a list of bonded bluetooth devices and connecting them.
 */
public class DevicesActivity extends AppCompatActivity {

    private ListView listView;
    private int connectedIndex = -1;
    private ArrayList<String> bondedDevices;
    private Boolean isConnecting = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.deviceListView);

        BluetoothHandler bluetoothHandler = BluetoothHandler.getInstance();

        if(!bluetoothHandler.initialize()) {
            Toast.makeText(getApplicationContext(), getString(R.string.bluetooth_not_supported), Toast.LENGTH_SHORT).show();
        }
        else{
            if(!bluetoothHandler.isEnabled())
                startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 0);

            bondedDevices = bluetoothHandler.getDevices();

            if(bondedDevices.isEmpty())
                Toast.makeText(getApplicationContext(),getString(R.string.no_bonded_devices),Toast.LENGTH_SHORT).show();
            else {

                for (int i = 0; i < bondedDevices.size(); i++) {

                    String itemValue = bondedDevices.get(i);

                    if(itemValue.equals(bluetoothHandler.getConnectedDevice()))
                    {
                        connectedIndex = i;
                        bondedDevices.set(i, bondedDevices.get(i) + " (" + getString(R.string.connected) + ")");
                        break;
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, bondedDevices);

                // Assign adapter to ListView
                listView.setAdapter(adapter);
            }

        }

        listView.setOnItemClickListener(clickListener);
    }

    /**
     * List view item click handler
     */
    private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {

            if(isConnecting)
                return;

            isConnecting = true;

            final String clickedDeviceName = (String) listView.getItemAtPosition(position);
            final View parentView = parent;
            final TextView clickedTextView = (TextView) view;
            final int clickedIndex = position;

            Toast.makeText(getApplicationContext(), getString(R.string.connecting_to) + " " + clickedDeviceName, Toast.LENGTH_SHORT).show();

            clickedTextView.setText(bondedDevices.get(clickedIndex) + " (" + getString(R.string.connecting) + "...)");

            new Thread(new Runnable() {
                public void run() {

                    // a potentially time consuming task
                    final Boolean connected = BluetoothHandler.getInstance().connect(clickedDeviceName);

                    parentView.post(new Runnable() {
                        public void run() {

                            if(connected && connectedIndex != clickedIndex) {
                                clickedTextView.setText(bondedDevices.get(clickedIndex) + " (" + getString(R.string.connected) + ")");
                                connectedIndex = clickedIndex;
                                String message = parentView.getContext().getString(R.string.connected_to) + " " + clickedDeviceName;
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String message = parentView.getContext().getString(R.string.connection_to) + " " + clickedDeviceName + " " + getString(R.string.failed);
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                clickedTextView.setText(bondedDevices.get(clickedIndex));
                            }

                            isConnecting = false;
                            listView.invalidate();

                        }
                    });
                }
            }).start();
        }
    };

}
