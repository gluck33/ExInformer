package ru.openitr.exinformer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by
 * User: oleg
 * Date: 17.07.13
 * Time: 16:39
 */
public class BasePreferencesActivity extends PreferenceActivity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.base_preferences);
        //TextView summary = findViewById(R.xml.base_preferences);

    }


}
