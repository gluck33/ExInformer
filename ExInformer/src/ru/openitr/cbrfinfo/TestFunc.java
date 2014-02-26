package ru.openitr.cbrfinfo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

/**
 * Created by oleg on 20.02.14.
 */
public class TestFunc {

    public static void rollbackToYesterday(Context context){
        ContentResolver cr = context.getContentResolver();
        ContentValues cv = new ContentValues();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Long lastSavedData = sharedPreferences.getLong("PREF_LAST_DATE", 0);
        Calendar onDate = Calendar.getInstance();
        onDate.setTimeInMillis(lastSavedData);
        onDate.roll(Calendar.DAY_OF_YEAR, -1);
        cv.put(CbInfoDb.CUR_KEY_DATE, onDate.getTimeInMillis());
        cr.update(CBInfoProvider.CURRENCY_CONTENT_URI ,cv,null,null);
        cv.clear();
        cv.put(CbInfoDb.MET_KEY_DATE,onDate.getTimeInMillis());
        cr.update(CBInfoProvider.METAL_CONTENT_URI,cv, null, null);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("PREF_LAST_DATE", onDate.getTimeInMillis());
        editor.commit();
        Intent widgetUpdateIntent = new Intent(InfoWidget.INFO_WIDGET_UPDATE);
        widgetUpdateIntent.putExtra("CURS_TIME", onDate.getTimeInMillis());
        context.sendBroadcast(widgetUpdateIntent);
    }
}
