package ru.openitr.cbrfinfo;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;

import java.sql.Date;
import java.text.SimpleDateFormat;


/**
 * Created by
 * User: oleg
 * Date: 05.04.13
 * Time: 16:16
 */
public class CurrencyWidget extends AppWidgetProvider {

    static final Uri CURRENCY_URI = CBInfoProvider.CURRENCY_CONTENT_URI;
    public static String CURRENCY_WIDGET_UPDATE = "ru.openitr.cbrfinfo.CURRENCY_UPDATED";
    public Long cursTime = Long.valueOf(0);

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
           cursTime = intent.getLongExtra("CURS_TIME",1000*60*60*24);
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
            Cursor cursor = context.getContentResolver().query(CURRENCY_URI, CbInfoDb.CUR_ALL_COLUMNS, CbInfoDb.CUR_KEY_CHARCODE +" = ?", new String[]{_vChCode}, null);
            if (cursor.getCount()<=0) {
                Intent startServiceIntent = new Intent (context, CurInfoRefreshService.class);
                context.startService(startServiceIntent);
            }
            try {
                cursor.moveToFirst();
                String vChCode = cursor.getString(CbInfoDb.VALCHARCODE_COLUMN);
                String uriString = "android.resource://ru.openitr.cbrfinfo/drawable/f_"+vChCode;
                float curs = cursor.getFloat(CbInfoDb.VALCURS_COLUMN);
                long cursDate = cursor.getLong(CbInfoDb.VALDATE_COLUMN);
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                widgetView.setTextViewText(R.id.widgetVchCode,vChCode);
                widgetView.setTextViewText(R.id.widgetVCurs,String.valueOf(curs));
                widgetView.setImageViewUri(R.id.flagImageView, Uri.parse(uriString.toLowerCase()));
                widgetView.setTextViewText(R.id.cursDateTv,sdf.format(new Date(cursDate)));

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
         LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, "Widget: Widget info updated.");
    }
}