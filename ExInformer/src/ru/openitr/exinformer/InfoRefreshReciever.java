package ru.openitr.exinformer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: oleg
 * Date: 11.05.13
 * Time: 12:55
 *
 */
public class InfoRefreshReciever extends BroadcastReceiver  {
    public static final String ACTION_REFRESH_INFO_ALARM = "ru.openitr.exinformer.ACTION_REFRESH_INFO_ALARM";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startIntent = new Intent (context, InfoRefreshService.class);
        context.startService(startIntent);
        Log.d(MainActivity.LOG_TAG, "InfoRefreshReciever: Recieve 'ru.openitr.exinformer.ACTION_REFRESH_INFO_ALARM'");
    }
}
