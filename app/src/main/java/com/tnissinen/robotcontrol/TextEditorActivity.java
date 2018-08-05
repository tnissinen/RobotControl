package com.tnissinen.robotcontrol;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

/**
 * Activity for editing text in a fullscreen view
 */
public class TextEditorActivity extends AppCompatActivity {

    public static final String INIT_TEXT_KEY = "initText";
    public static final String RESULT_TEXT_KEY = "resultText";

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editText = findViewById(R.id.textEdit);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.containsKey(INIT_TEXT_KEY))
            editText.setText(bundle.getString(INIT_TEXT_KEY));

        editText.setSelection(editText.getText().length());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                // put the String to pass back into an Intent and close this activity
                Intent intent = new Intent();
                intent.putExtra(RESULT_TEXT_KEY, editText.getText());

                setResult(RESULT_OK, intent);
                finish();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
