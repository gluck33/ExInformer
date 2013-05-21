package ru.openitr.exinformer;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;


/**
 * Created by
 * User: oleg
 * Date: 05.04.13
 * Time: 16:16
 */
public class CurrencyWidget extends AppWidgetProvider {

    static final Uri CURRENCY_URI = Uri.parse("content://ru.openitr.exinformer.currency/currencys");
    public static String CURRENCY_WIDGET_UPDATE = "ru.openitr.exinformer.CURRENCY_UPDATED";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        SharedPreferences.Editor editor = context.getSharedPreferences(CurWidgetConfActivity.WIDGET_PREF,Context.MODE_PRIVATE).edit();
        for (int widgetId: appWidgetIds){
            editor.remove(CurWidgetConfActivity.WIDGET_CURRENCY_CHARCODE +widgetId);
        }
        editor.commit();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (CURRENCY_WIDGET_UPDATE.equals(intent.getAction())){
           updateWidgets(context);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        SharedPreferences sp = context.getSharedPreferences(CurWidgetConfActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        for (int id: appWidgetIds){
            updateWidget(context, appWidgetManager, sp, id);
        }
    }

    static void updateWidget(Context context, AppWidgetManager appWidgetManager, SharedPreferences sp, int id) {
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.cur_widget);
        String _vChCode = sp.getString(CurWidgetConfActivity.WIDGET_CURRENCY_CHARCODE +id, null);
        if (_vChCode != null){
            Cursor cursor = context.getContentResolver().query(CURRENCY_URI, CurrencyDbAdapter.ALL_COLUMNS, CurrencyDbAdapter.KEY_CHARCODE+" = ?", new String[]{_vChCode}, null);
            try {
                cursor.moveToFirst();
                String vChCode = cursor.getString(CurrencyDbAdapter.VALCHARCODE_COLUMN);
                String uriString = "android.resource://ru.openitr.exinformer/drawable/f_"+vChCode;
                int nominal = cursor.getInt(CurrencyDbAdapter.VALNOMINAL_COLUMN);
                float cur = cursor.getFloat(CurrencyDbAdapter.VALCURS_COLUMN);
                widgetView.setTextViewText(R.id.widgetVchCode,vChCode);
                widgetView.setTextViewText(R.id.widgetVCurs,String.valueOf(cur/nominal));
                widgetView.setImageViewUri(R.id.flagImageView, Uri.parse(uriString.toLowerCase()));


            }finally {
                cursor.close();
            }
            appWidgetManager.updateAppWidget(id, widgetView);
        }
    }
    public void updateWidgets(Context context){
        ComponentName thisWidget = new ComponentName(context, CurrencyWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetsIds = appWidgetManager.getAppWidgetIds(thisWidget);
        SharedPreferences sp = context.getSharedPreferences(CurWidgetConfActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        for (int widgetId: appWidgetsIds){
            updateWidget(context,appWidgetManager,sp,widgetId);
        }
        if (main.DEBUG) Log.d(main.LOG_TAG, "Widget: Widget info updated.");
    }
}
