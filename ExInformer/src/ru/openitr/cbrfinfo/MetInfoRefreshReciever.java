package ru.openitr.cbrfinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: oleg
 * Date: 11.05.13
 * Time: 12:55
 */
public class MetInfoRefreshReciever extends BroadcastReceiver {
    public static final String ACTION_REFRESH_MET_INFO_ALARM = "ru.openitr.cbrfinfo.ACTION_REFRESH_MET_INFO_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + "getAction: "+intent.getAction());
        if (intent.getAction().equals(ACTION_REFRESH_MET_INFO_ALARM)) {
            Intent metInfoServiceIntent = new Intent(context, MetInfoRefreshService.class);
            context.startService(metInfoServiceIntent);
        }

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){
            Intent metInfoServiceIntent = new Intent(context, MetInfoRefreshService.class);
            metInfoServiceIntent.putExtra(MainActivity.PARAM_ONLY_SET_ALARM, true);
            context.startService(metInfoServiceIntent);

        }
    }
}
