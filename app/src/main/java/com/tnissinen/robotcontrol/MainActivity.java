package com.tnissinen.robotcontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Thread.sleep;

/*

    TODO:
    - simulaatiot parannus?


 */
/**
 * Main activity of the application. Draws controls for adjusting servo rotations, buttons for memory slots and tab view for log and route fragments.
 */
public class MainActivity extends AppCompatActivity {

    private ArrayList<SeekBar> seekBars;
    private ArrayList<SimulationView> simulationViews;
    private MainActivityFragment mainFragment;
    private MenuItem devicesMenuItem;
    private HashMap<Button, Integer[]> memorySlots;
    private int[] rotationStarts;
    private int[] rotationMiddlePoints;
    private float[] rotationMultipliers;
    private int routeDelay;
    private ArrayList<TextView> servoTextViews;
    private TabLayout tabMenuBar;
    private ViewPager tabViewPager;
    private LogFragment logFragment;
    private RouteFragment routeFragment;

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(BluetoothHandler.getInstance().isConnected())
            devicesMenuItem.setIcon(R.drawable.ic_bluetooth_connected_black_24dp);
        else
            devicesMenuItem.setIcon(R.drawable.ic_bluetooth_black_24dp);

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set preferences default values
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);


        // listen to preferences changes
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(
                        preferencesChangeListener);

        mainFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment);

        memorySlots = new HashMap<>();

        for (int i = 0; i < 6; i++){
            Button button = findViewById(getResources().getIdentifier("memButton" + (i + 1), "id", getPackageName()));
            button.setOnClickListener(memButtonClickListener);
            button.setOnLongClickListener(memButtonLongClickListener);
        }

        seekBars = new ArrayList<>();
        seekBars.add((SeekBar) findViewById(R.id.servo1SeekBar));
        seekBars.add((SeekBar) findViewById(R.id.servo2SeekBar));
        seekBars.add((SeekBar) findViewById(R.id.servo3SeekBar));
        seekBars.add((SeekBar) findViewById(R.id.servo4SeekBar));

        for (SeekBar seekBar : seekBars)
            seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        simulationViews = new ArrayList<>();
        simulationViews.add((SimulationView) findViewById(R.id.simuView1));
        simulationViews.add((SimulationView) findViewById(R.id.simuView2));
        simulationViews.add((SimulationView) findViewById(R.id.simuView3));
        simulationViews.add((SimulationView) findViewById(R.id.simuView4));

        servoTextViews = new ArrayList<>();
        servoTextViews.add((TextView)findViewById(R.id.servo1TextView));
        servoTextViews.add((TextView)findViewById(R.id.servo2TextView));
        servoTextViews.add((TextView)findViewById(R.id.servo3TextView));
        servoTextViews.add((TextView)findViewById(R.id.servo4TextView));

        logFragment = new LogFragment();
        routeFragment = new RouteFragment();

        tabMenuBar = findViewById(R.id.tabMenu);
        tabViewPager = findViewById(R.id.tabContent);

        tabViewPager.setAdapter(new SectionsPagerAdapter(mainFragment.getChildFragmentManager()));
        tabMenuBar.setupWithViewPager(tabViewPager);

        routeFragment.setRouteStartedListener(new RouteFragment.OnRouteStartedListener() {
            public void onRouteStarted(String route) {
                runRoute(route);
            }
        });

        setPreferences();

        routeFragment.setCurrentServoValues(formatCommand(getRotationValuesFromSeekBars()));
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the tabs.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if(position == 1)
                return routeFragment;
            else
                return logFragment;
        }


        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {

            if(position == 1)
                return getString(R.string.routes);
            else
                return getString(R.string.log);
        }
    }

    private void setLayoutEnabled(boolean enabled){

        setChildButtonsEnabled((LinearLayout)findViewById(R.id.memButtons), enabled);
        setChildButtonsEnabled((LinearLayout)findViewById(R.id.routeButtons), enabled);
        setChildButtonsEnabled((LinearLayout)findViewById(R.id.actionButtons), enabled);

        for(SeekBar seekBar : seekBars)
            seekBar.setEnabled(enabled);

        TabLayout tabMenu = findViewById(R.id.tabMenu);
        tabMenu.setEnabled(enabled);
    }

    private void setChildButtonsEnabled(ViewGroup viewGroup, boolean enabled){

        if(viewGroup == null)
            return;

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof Button)
                child.setEnabled(enabled);
        }
    }

    private void runRoute(String routeString){

        setLayoutEnabled(false);

        final View parentView = mainFragment.getView();
        final ArrayList<Integer[]> rotationList = new ArrayList<>();
        final String lines[] = routeString.split("\\r?\\n");

        for(int i = 0; i < lines.length; i++) {

            String parts[] = lines[i].split(",");

            if(parts.length == 4){

                Integer[] values = new Integer[parts.length];

                for(int j = 0; j < parts.length; j++)
                    values[j] = Integer.parseInt(parts[j]);

                rotationList.add(values);
            }
        }

        if(!rotationList.isEmpty())
        {
            Toast.makeText(getApplicationContext(), getString(R.string.running_route), Toast.LENGTH_SHORT).show();

            new Thread(new Runnable() {
                public void run() {

                    for(int i = 0; i < rotationList.size(); i++){

                        final int iter = i;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Integer[] progresses = getProgressFromValues(rotationList.get(iter));
                                setSeekBarProgresses(progresses);
                                setRotations(getRotationValuesFromSeekBars());
                            }
                        });


                        try {
                            sleep(routeDelay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                    parentView.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.route_ended), Toast.LENGTH_SHORT).show();
                            setLayoutEnabled(true);
                        }
                    });
                }
            }).start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        invalidateOptionsMenu();
    }

    /*
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putBoolean("MyBoolean", true);
        savedInstanceState.putDouble("myDouble", 1.9);
        savedInstanceState.putInt("MyInt", 1);
        savedInstanceState.putString("MyString", "Welcome back to Android");
        // etc.
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        devicesMenuItem = menu.findItem(R.id.action_devices);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings)
            startActivity(new Intent(this, SettingsActivity.class));
        else if(id == R.id.action_devices)
            startActivity(new Intent(this, DevicesActivity.class));

        return super.onOptionsItemSelected(item);
    }

    //kuuntelija muutoksille SharedPreferences:ssa
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                    MainActivityFragment controlFragment = (MainActivityFragment)
                            getSupportFragmentManager().findFragmentById(
                                    R.id.mainFragment);

                    setPreferences();
                }
            };

    private void setPreferences(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        rotationStarts = new int[4];
        rotationMultipliers = new float[4];
        rotationMiddlePoints = new int[4];

        for (int i = 0; i < 4; i++){

            String index = "" + (i + 1);

            String servoName = preferences.getString("servo_name" + index, getString(getResources().getIdentifier("servo" + index, "string", getPackageName())));
            servoTextViews.get(i).setText(servoName);

            Integer start = new Integer(preferences.getString("servo_rotation_start" + index, "0"));
            Integer end = new Integer(preferences.getString("servo_rotation_end" + index, "0"));

            rotationStarts[i] = start;
            int range = end - start;
            rotationMultipliers[i] = (float)range / 100;
            rotationMiddlePoints[i] = rotationStarts[i] + (range / 2);
        }

        routeDelay = new Integer(preferences.getString("route_delay", "1")) * 1000;

    }


    private View.OnClickListener memButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Integer[] values = memorySlots.get(view);

            if(values != null) {

                setSeekBarProgresses(values);

                setRotations(getRotationValuesFromSeekBars());
            }
        }
    };

    private Integer[] getRotationValuesFromSeekBars(){

        Integer[] values = new Integer[4];

        for (int i = 0; i < seekBars.size(); i++)
            values[i] = rotationStarts[i] + (int)(seekBars.get(i).getProgress() * rotationMultipliers[i]);

        return values;
    }

    private Integer[] getProgressFromValues(Integer[] values){

        Integer[] progresses = new Integer[4];

        progresses[0] = (int)((values[0] - rotationStarts[0]) / rotationMultipliers[0]);
        progresses[1] = (int)((values[1] - rotationStarts[1]) / rotationMultipliers[1]);
        progresses[2] = (int)((values[2] - rotationStarts[2]) / rotationMultipliers[2]);
        progresses[3] = (int)((values[3] - rotationStarts[3]) / rotationMultipliers[3]);

        return progresses;
    }

    private View.OnLongClickListener memButtonLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {

            Integer[] progresses = new Integer[4];

            for (int i = 0; i < seekBars.size(); i++)
                progresses[i] = seekBars.get(i).getProgress();

            memorySlots.put((Button)view, progresses);

            String memorySetText = getString(R.string.memory_slot_set, ((Button) view).getText());
            Toast.makeText(getApplicationContext(), memorySetText, Toast.LENGTH_SHORT).show();

            return true;
        }
    };

    private String formatCommand(Integer[] values){
        return values[0] + "," + values[1] + "," + values[2] + "," + values[3];
    }

    private void setSeekBarProgresses(Integer[] values){

        for (int i = 0; i < seekBars.size(); i++)
            seekBars.get(i).setProgress(values[i]);
    }

    private void setRotations(Integer[] values){

        String newServoCommand = formatCommand(values);

        logFragment.addLine(newServoCommand);

        for (int i = 0; i < simulationViews.size(); i++)
            simulationViews.get(i).setRotationsAndUpdate(values[i] - rotationMiddlePoints[i], 0);

        routeFragment.setCurrentServoValues(newServoCommand);

        if(BluetoothHandler.getInstance().isConnected()) {

            Boolean sendOK = BluetoothHandler.getInstance().sendMessage(newServoCommand);

            if (!sendOK) {
                String errorMsg = getString(R.string.msg_sending_failed) + ": " + BluetoothHandler.getInstance().getErrorMessage();
                logFragment.addLine(errorMsg);
            }
        }
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if(fromUser)
                setRotations(getRotationValuesFromSeekBars());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}

    };

}
