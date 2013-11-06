package ru.openitr.cbrfinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: oleg
 * Date: 11.05.13
 * Time: 12:55
 *
 */
public class CurInfoRefreshReciever extends BroadcastReceiver  {
    public static final String ACTION_REFRESH_INFO_ALARM = "ru.openitr.cbrfinfo.ACTION_REFRESH_MET_INFO_ALARM";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startIntent = new Intent (context, CurInfoRefreshService.class);
        context.startService(startIntent);
        LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this,"'ru.openitr.cbrfinfo.ACTION_REFRESH_MET_INFO_ALARM'");
    }
}
