package ru.openitr.cbrfinfo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
        int drc = cr.delete(CBInfoProvider.CURRENCY_CONTENT_URI, CbInfoDb.CUR_KEY_DATE + " > ?",
                new String[] {Long.toString(onDate.getTimeInMillis())});
        int drm = cr.delete(CBInfoProvider.METAL_CONTENT_URI, CbInfoDb.MET_KEY_DATE + " > " + Long.toString(onDate.getTimeInMillis()), null);
//        int drc = cr.delete(CBInfoProvider.CURRENCY_CONTENT_URI, null, null);
//        int drm = cr.delete(CBInfoProvider.METAL_CONTENT_URI, null, null);
        if (LogSystem.DEBUG) {
            LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, "Rollback to yesterday. Deleted :" + Integer.toString(drc) + " in currencys.");
            LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, "Rollback to yesterday. Deleted :"+Integer.toString(drm) + " in metalls.");
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("PREF_LAST_DATE", onDate.getTimeInMillis());
        editor.commit();
        Intent widgetUpdateIntent = new Intent(InfoWidget.INFO_WIDGET_UPDATE);
        widgetUpdateIntent.putExtra("CURS_TIME", onDate.getTimeInMillis());
        context.sendBroadcast(widgetUpdateIntent);

    }
}
