package ru.openitr.cbrfinfo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.TextView;


/**
 * Created by
 * User: Oleg Balditsyn
 * Date: 05.04.13
 * Time: 16:16
 */

public class InfoWidget extends AppWidgetProvider {

    static final Uri CURRENCY_URI = CBInfoProvider.CURRENCY_CONTENT_URI;
    public static String INFO_WIDGET_UPDATE = "ru.openitr.cbrfinfo.INFO_UPDATED";
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
        SharedPreferences.Editor editor = context.getSharedPreferences(WidgetConfActivity.WIDGET_PREF,Context.MODE_PRIVATE).edit();
        for (int widgetId: appWidgetIds){
            editor.remove(WidgetConfActivity.WIDGET_CURRENCY_CHARCODE + Integer.toString(widgetId));
            editor.remove(WidgetConfActivity.WIDGET_METAL_CODE + Integer.toString(widgetId));
            editor.remove(WidgetConfActivity.WIDGET_INFO_TYPE + Integer.toString(widgetId));
        }
        editor.commit();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (INFO_WIDGET_UPDATE.equals(intent.getAction())){
           updateWidgets(context);
           cursTime = intent.getLongExtra("CURS_TIME",1000*60*60*24);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        SharedPreferences sp = context.getSharedPreferences(WidgetConfActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        for (int id: appWidgetIds){
            updateWidget(context, appWidgetManager, sp, id);
        }
    }

    private RemoteViews inflateWidget(Context context, Icurrency cur){
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.cur_widget);
        if (cur.getvCurs() == 0)
            getCurrentInfo(context, 0);
        widgetView.setTextViewText(R.id.widgetVchCode,cur.getVchCode());
        widgetView.setTextViewText(R.id.widgetVCurs,cur.vCursAsString());
        String uriString = "android.resource://" + context.getPackageName() +"/drawable/f_";
        widgetView.setImageViewUri(R.id.flagImageView, Uri.parse(uriString +cur.getVchCode().toLowerCase()));
        widgetView.setTextViewText(R.id.cursDateTv,cur.vDateAsString());
        return widgetView;
    }

    private RemoteViews inflateWidget(Context context, DragMetal met){
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.cur_widget);
        if (met.getPrice() == 0)
            getCurrentInfo(context,1);
        String[] metNames = context.getResources().getStringArray(R.array.metall_names);
        widgetView.setTextViewText(R.id.widgetVchCode,met.getMetallSymName());
        widgetView.setTextViewText(R.id.widgetVCurs,Float.toString(met.getPrice()));
        String uriString = "android.resource://" + context.getPackageName() +"/drawable/";
        widgetView.setImageViewUri(R.id.flagImageView, Uri.parse(uriString + met.getMetallEngName() + "_w"));
        widgetView.setTextViewText(R.id.cursDateTv,met.getOnDateAsString());
        return widgetView;
    }

    private void setOnClickPendingIntent(Context context, int infoType, RemoteViews remoteViews, int widgetId){
        Intent showMainActivityIntent = new Intent(context, MainActivity.class);
        showMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        showMainActivityIntent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        showMainActivityIntent.putExtra("CURRENT_PAGE",infoType);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, widgetId,showMainActivityIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.flagImageView, pendingIntent);
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, SharedPreferences sp, int id) {
        String stringInfoType = sp.getString(WidgetConfActivity.WIDGET_INFO_TYPE + id, null);
        RemoteViews remoteView = null;
        if (stringInfoType == null) return;
        Integer infoType = Integer.decode(stringInfoType);
        switch (infoType){
            case  0:
                String _vChCode = sp.getString(WidgetConfActivity.WIDGET_CURRENCY_CHARCODE  + id, null);
                if (_vChCode == null) break;
                Icurrency cur = Icurrency.getIcurencyFromBase(context, _vChCode);
                if (cur !=null)
                    remoteView = inflateWidget(context, cur);
                    setOnClickPendingIntent(context, infoType,remoteView, id);
                    appWidgetManager.updateAppWidget(id, remoteView);
                break;
            case 1:
                String prefName = WidgetConfActivity.WIDGET_METAL_CODE + id;
                String prefStringValue = sp.getString(prefName, null);
                Integer _metCode = Integer.parseInt(prefStringValue);
//                        Integer.getInteger(prefStringValue);
                if (_metCode == null) break;
                DragMetal met = DragMetal.getMetalFromBase(context, _metCode);
                if (met != null)
                    remoteView = inflateWidget(context, met);
                    setOnClickPendingIntent(context, infoType,remoteView, id);
                    appWidgetManager.updateAppWidget(id, remoteView);
                break;
            }
    }

    public void updateWidgets(Context context){
        ComponentName thisWidget = new ComponentName(context, InfoWidget.class.getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetsIds = appWidgetManager.getAppWidgetIds(thisWidget);
        SharedPreferences sp = context.getSharedPreferences(WidgetConfActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        for (int widgetId: appWidgetsIds){
            updateWidget(context,appWidgetManager,sp,widgetId);
            LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, "Update widget id = "+String.valueOf(widgetId));
        }
         LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, "Widget: Widget info updated.");
    }
    private void getCurrentInfo (Context context, int infoType){
        switch (infoType){
            case 0:
                Intent refreshCurServiceIntent = new Intent(context, CurInfoRefreshService.class);
                refreshCurServiceIntent.putExtra(CurrencyInfoFragment.PARAM_FROM_ACTIVITY, true);
                context.startService(refreshCurServiceIntent);
                break;
            case 1:
                Intent refreshMetInfoService = new Intent(context, MetInfoRefreshService.class);
                refreshMetInfoService.putExtra(CurrencyInfoFragment.PARAM_FROM_ACTIVITY, true);
                context.startService(refreshMetInfoService);
                break;
        }
    }

}

class myRemoteViews extends RemoteViews{

    public myRemoteViews(String packageName, int layoutId) {
        super(packageName, layoutId);
    }

}