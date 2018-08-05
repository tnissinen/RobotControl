package com.tnissinen.robotcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Fragment for managing movement routes of several servo rotation1 points.
 */
public class RouteFragment extends Fragment {

    public static final Integer REQUEST_CODE = 0;

    private String currentServoValues = "";
    private ArrayList<ToggleButton> routeButtons;
    private HashMap<Integer, String> routes;
    private ToggleButton selectedRouteButton;
    private OnRouteStartedListener routeStartedListener;
    private TextView routeText;

    public RouteFragment(){
        routes = new HashMap<>();
    }

    /**
     * Interface for route started events
     */
    public interface OnRouteStartedListener {
        void onRouteStarted(String route);
    }

    public void setRouteStartedListener(OnRouteStartedListener eventListener) {
        routeStartedListener = eventListener;
    }

    /**
     * Sets current servo values to be used in route management
     * @param servoValues
     */
    public void setCurrentServoValues(String servoValues) {
        currentServoValues = servoValues;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result =  inflater.inflate(R.layout.fragment_route, container, false);

        routeText = result.findViewById(R.id.routeEditText);
        routeText.setMovementMethod(new ScrollingMovementMethod());
        routeText.setOnLongClickListener(routeTextLongClickListener);

        routeButtons = new ArrayList<>();
        routeButtons.add((ToggleButton) result.findViewById(R.id.r1Button));
        routeButtons.add((ToggleButton) result.findViewById(R.id.r2Button));
        routeButtons.add((ToggleButton) result.findViewById(R.id.r3Button));
        routeButtons.add((ToggleButton) result.findViewById(R.id.r4Button));

        for (ToggleButton button : routeButtons)
            button.setOnClickListener(routeButtonListener);

        selectedRouteButton = routeButtons.get(0);

        Button addButton = result.findViewById(R.id.addButton);
        Button editButton = result.findViewById(R.id.editButton);
        Button clearButton = result.findViewById(R.id.clearButton);
        Button runButton = result.findViewById(R.id.runButton);

        addButton.setOnClickListener(actionButtonListener);
        clearButton.setOnClickListener(actionButtonListener);
        editButton.setOnClickListener(actionButtonListener);
        runButton.setOnClickListener(actionButtonListener);

        return result;
    }

    private void startTextEditActivity(){

        Intent preferencesIntent = new Intent(getActivity(), TextEditorActivity.class);
        preferencesIntent.putExtra(TextEditorActivity.INIT_TEXT_KEY, routeText.getText().toString());
        startActivityForResult(preferencesIntent, REQUEST_CODE);
    }

    private View.OnLongClickListener routeTextLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {

            startTextEditActivity();
            return true;
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check that it is the text edit activity with an OK result
        if (requestCode == REQUEST_CODE) {
            if (resultCode == DevicesActivity.RESULT_OK) {

                Bundle bundle = data.getExtras();

                if(bundle != null)
                {
                    Object editedTextObject = bundle.get(TextEditorActivity.RESULT_TEXT_KEY);

                    if(editedTextObject != null){
                        String editedText = editedTextObject.toString();
                        routeText.setText(editedText);
                        routes.put(selectedRouteButton.getId(), editedText);
                    }
                }
            }
        }
    }

    private View.OnClickListener routeButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(v.getId() == selectedRouteButton.getId())
                return;

            selectedRouteButton = (ToggleButton)v;
            selectedRouteButton.setChecked(true);

            for (ToggleButton button : routeButtons)
                if(button.getId() != v.getId())
                    button.setChecked(false);

            String route = routes.get(selectedRouteButton.getId());
            routeText.setText(route);
        }
    };

    private View.OnClickListener actionButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.clearButton:
                    setAndSaveRouteText("");
                    break;
                case R.id.addButton:
                    addNewLineToRouteText(currentServoValues);
                    break;
                case R.id.editButton:
                    startTextEditActivity();
                    break;
                case R.id.runButton:
                    if(routeStartedListener != null)
                        routeStartedListener.onRouteStarted(routeText.getText().toString());
                    break;
            }
        }
    };

    private void addNewLineToRouteText(String newLine){

        String line = "";

        if(routeText.getText().length() > 0)
            line += routeText.getText() + "\n";

        line += newLine;

        setAndSaveRouteText(line);
    }

    private void setAndSaveRouteText(String route){
        routeText.setText(route);
        routes.put(selectedRouteButton.getId(), route);
    }

}
