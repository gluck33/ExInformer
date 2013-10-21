package ru.openitr.cbrfinfo;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import ru.openitr.cbrfinfo.R;

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
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        this.setResult(Activity.RESULT_OK);
    }
}
