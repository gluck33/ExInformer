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
public class CurInfoRefreshReciever extends BroadcastReceiver {
    public static final String ACTION_REFRESH_INFO_ALARM = "ru.openitr.cbrfinfo.ACTION_REFRESH_CUR_INFO_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + "getAction: "+intent.getAction());
        Intent curInfoServiceIntent = new Intent(context, CurInfoRefreshService.class);
        if (intent.getAction().equals(ACTION_REFRESH_INFO_ALARM)) {
            context.startService(curInfoServiceIntent);
        }
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){
            curInfoServiceIntent.putExtra(MainActivity.PARAM_ONLY_SET_ALARM, true);
            context.startService(curInfoServiceIntent);
        }
    }
}
