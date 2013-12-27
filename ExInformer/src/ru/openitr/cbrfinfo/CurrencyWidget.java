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

import java.lang.reflect.Array;
import java.sql.Date;
import java.text.SimpleDateFormat;


/**
 * Created by
 * User: Oleg Balditsyn
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

    static RemoteViews inflateWidget(Context context, Icurrency cur){
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.cur_widget);
        widgetView.setTextViewText(R.id.widgetVchCode,cur.getVchCode());
        widgetView.setTextViewText(R.id.widgetVCurs,cur.vCursAsString());
        widgetView.setImageViewUri(R.id.flagImageView, Uri.parse("android.resource://ru.openitr.exinformer/drawable/f_" +cur.getVchCode().toLowerCase()));
        widgetView.setTextViewText(R.id.cursDateTv,cur.vDateAsString());
        return widgetView;
    }

    static RemoteViews inflateWidget(Context context, DragMetal met){
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.cur_widget);
        String[] metNames = context.getResources().getStringArray(R.array.metall_names);
        widgetView.setTextViewText(R.id.widgetVchCode,met.getMetallSymName());
        widgetView.setTextViewText(R.id.widgetVCurs,Float.toString(met.getPrice()));
        widgetView.setImageViewUri(R.id.flagImageView, Uri.parse("android.resource://ru.openitr.exinformer/drawable/" + met.getMetallEngName()));
        widgetView.setTextViewText(R.id.cursDateTv,met.getOnDateAsString());
        return widgetView; 
    }

    static void updateWidget(Context context, AppWidgetManager appWidgetManager, SharedPreferences sp, int id) {
        String stringInfoType = sp.getString(CurWidgetConfActivity.WIDGET_INFO_TYPE + id, null);
        if (stringInfoType == null) return;
        Integer infoType = Integer.decode(stringInfoType);
        switch (infoType){
            case  0:
                String _vChCode = sp.getString(CurWidgetConfActivity.WIDGET_CURRENCY_CHARCODE +id, null);
                if (_vChCode == null) break;
                Icurrency cur = Icurrency.getIcurencyFromBase(context, _vChCode);
                if (cur !=null)
                    appWidgetManager.updateAppWidget(id, inflateWidget(context, cur));
                break;
            case 1:
                Integer _metCode = Integer.getInteger(sp.getString(CurWidgetConfActivity.WIDGET_METAL_CODE+id, null));
                if (_metCode == null) break;
                DragMetal met = DragMetal.getMetalFromBase(context, _metCode);
                if (met != null)
                    appWidgetManager.updateAppWidget(id, inflateWidget(context, met));
                break;
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
