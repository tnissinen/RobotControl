package com.tnissinen.robotcontrol;

import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment for showing application settings (loaded from resource file)
 */
public class SettingsActivityFragment extends PreferenceFragment {

    public SettingsActivityFragment() {
    }

    // luo preferences käyttöliittymän tiedoston prefrences.xml avulla joka res/xml kansiossa
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences); // ladataan XML:sta

    }


}
