package ru.openitr.exinformer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

/**
 * Created by
 * User: oleg
 * Date: 17.07.13
 * Time: 16:23
 */
public class TimePreference extends DialogPreference{
    private TimePicker timePicker;
    private static final int DEFAULT_HOUR = 8;
    private static final int DEFAULT_MINUTE = 0;

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        int hour = p.getInt(getKey()+".hour",0);
        int minute = p.getInt(getKey()+".minute",0);
        setSummary(String.format("%d:%02d",hour,minute));

//        setSummary(getTimeString());
    }

    @Override
    public void onBindDialogView(View view) {
        timePicker = (TimePicker) view.findViewById(R.id.prefTimePicker);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(getSharedPreferences().getInt(getKey() + ".hour", DEFAULT_HOUR));
        timePicker.setCurrentMinute(getSharedPreferences().getInt(getKey() + ".minute", DEFAULT_MINUTE));
    }

    @Override
    public void onClick(DialogInterface dialog,
                        int button) {
        if (button == Dialog.BUTTON_POSITIVE) {
            SharedPreferences.Editor editor = getEditor();
            int hour = timePicker.getCurrentHour();
            int minute = timePicker.getCurrentMinute();
            editor.putInt(getKey() + ".hour", hour);
            editor.putInt(getKey() + ".minute", minute);
            editor.commit();
            setSummary(String.format("%d:%02d",hour,minute));
        }
    }

}
