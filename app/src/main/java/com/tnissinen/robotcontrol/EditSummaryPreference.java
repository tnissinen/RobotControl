package com.tnissinen.robotcontrol;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Class for creating preferences that show the current value in summary
 */
public class EditSummaryPreference extends EditTextPreference {


    public EditSummaryPreference(Context context) {
        super(context);
    }

    public EditSummaryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Returns the actual text value as the summary
     * {@inheritDoc}
     * @return summary value
     */
    @Override
    public CharSequence getSummary() {
        return getText();
    }
}