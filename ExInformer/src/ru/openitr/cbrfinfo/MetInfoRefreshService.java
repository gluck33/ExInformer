package ru.openitr.cbrfinfo;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by
 * User: Oleg Balditsyn
 * Date: 24.04.13
 * Time: 9:41
 * Сервис запрашивает информацию о ценах на драгметаллы с сервера cbr.ru и помещает её в БД.
 */
public class MetInfoRefreshService extends Service {

    AlarmManager alarms;
    PendingIntent alarmIntent;
    private boolean lastInfo = false;
    private boolean onlySetAlarm;
    private Notification newExchangeRateNotification;
    NotificationManager notificationManager;
    SharedPreferences sharedPreferences;
    public static final int NOTIFICATION_ID = 1;
    boolean soundNotification;
    int updateInterval;
    boolean fromActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this,"Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        lastInfo = false;
        Calendar onDate = Calendar.getInstance();
        Long dateFromExtraParam = intent.getLongExtra(MainActivity.PARAM_DATE,0);
        onDate.setTimeInMillis(dateFromExtraParam);
        onlySetAlarm = intent.getBooleanExtra(MainActivity.PARAM_ONLY_SET_ALARM, false);
        fromActivity = intent.getBooleanExtra(MainActivity.PARAM_FROM_ACTIVITY, false);
        LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "Service onStartCommand execute refresh task.");
        new refreshMetallInfoTask().execute(onDate);
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void notifyNewExchange(Calendar onDate){
        if (fromActivity) return;
        int icon = R.drawable.money;
        String tickerText = getString(R.string.exchange_rate_change);
        if (newExchangeRateNotification == null)
            newExchangeRateNotification = new Notification(icon, tickerText, System.currentTimeMillis());
        Context context = getApplicationContext();
        String expandedText = getString(R.string.obtained_change_in_exchange_rates);// + "  "+ ExtraCalendar.getSimpleDateString(onDate);
        String expandedTitle = tickerText;

        Intent startActivityIntent = new Intent(MetInfoRefreshService.this, CurrencyInfoFragment.class);

        PendingIntent launchIntent = PendingIntent.getActivity(context,0 ,startActivityIntent,0);

        newExchangeRateNotification.setLatestEventInfo(context,expandedTitle,expandedText,launchIntent);

        if (soundNotification)
            newExchangeRateNotification.defaults |= Notification.DEFAULT_SOUND;
        newExchangeRateNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(NOTIFICATION_ID, newExchangeRateNotification);

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
         LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this,"Destroy.");
    }

    private class refreshMetallInfoTask extends AsyncTask<Calendar, Integer, Integer> {
        private static final int OK = 20;
        private static final int STATUS_NETWORK_DISABLE = 30;
        private static final int STATUS_NOT_RESPOND = 40;
        private static final int STATUS_NO_DATA = 50;
        private static final int STATUS_NOT_FRESH_DATA = 60;
        private static final int STATUS_BAD_DATA = 70;
        SharedPreferences.Editor editor;
        private final String METALLS_URI = CBInfoProvider.METAL_CONTENT_URI.toString();
        boolean startFromNulldate = false;
        Calendar onDate;

        @Override
        protected Integer doInBackground(Calendar... params) {
            //return OK;
            if (onlySetAlarm){
                 LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "Only need update alarmSet");
                return OK;
            }
            onDate = Calendar.getInstance();
            if ((onDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY | onDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) && !fromActivity)
                return OK;
            if (params[0].getTimeInMillis() == 0 )
                startFromNulldate = true;
            else
                onDate = params[0];
                publishProgress();
                return getMetalOnDate(onDate, 4);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            Intent intent = new Intent(MainActivity.INFO_REFRESH_INTENT);
            intent.putExtra(MainActivity.PARAM_STATUS, MainActivity.STATUS_BEGIN_REFRESH);
            sendBroadcast(intent);
        }

        @Override
        protected void onPostExecute(Integer result) {
            LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "(onPostExecute) Result of service: " + result);
            super.onPostExecute(result);
            Intent resIntent = new Intent(CurrencyInfoFragment.INFO_REFRESH_INTENT);
            Intent widgetUpdateIntent = new Intent(CurrencyWidget.CURRENCY_WIDGET_UPDATE);
            switch (result) {
                case STATUS_NOT_RESPOND:
                    resIntent.putExtra(MainActivity.PARAM_STATUS, MainActivity.FIN_STATUS_NOT_RESPOND);
                    break;
                case STATUS_NETWORK_DISABLE:
                    resIntent.putExtra(MainActivity.PARAM_STATUS, MainActivity.FINS_STATUS_NETWORK_DISABLE);
                    break;
                case STATUS_NO_DATA:
                    resIntent.putExtra(MainActivity.PARAM_STATUS, MainActivity.FIN_STATUS_NO_DATA);
                    break;
                case STATUS_NOT_FRESH_DATA:
                    resIntent.putExtra(MainActivity.PARAM_STATUS, MainActivity.FIN_STATUS_OK);
                    break;
                case STATUS_BAD_DATA:
                    resIntent.putExtra(MainActivity.PARAM_STATUS, MainActivity.FIN_STATUS_NO_DATA);
                    break;
                default:
                    resIntent.putExtra(MainActivity.PARAM_STATUS, MainActivity.FIN_STATUS_OK);
            }

            sendBroadcast(resIntent);
            if (lastInfo & result == OK){
                 //LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "lastInfo = " + lastInfo);
                //widgetUpdateIntent.putExtra("CURS_TIME",Calendar.getInstance().getTimeInMillis());
                //sendBroadcast(widgetUpdateIntent);
                //notifyNewExchange(onDate);
            }
            stopSelf();
        }

        private int getMetalOnDate (Calendar toDate, int interval){
            Calendar fromDate = Calendar.getInstance();
            fromDate.setTimeInMillis(toDate.getTimeInMillis());
            fromDate.roll(Calendar.DAY_OF_YEAR, -interval);
            toDate.roll(Calendar.DAY_OF_YEAR, 1);
            return getMetalOnDateInterval(fromDate, toDate);
        }

        private int getMetalOnDate (Calendar onDate){
            return getMetalOnDateInterval(onDate, onDate);
        }

        private int getMetalOnDateInterval(Calendar fromDate, Calendar toDate) {
            ContentResolver cr = getContentResolver();
            DailyInfoStub dailyInfoStub = new DailyInfoStub();
            int res = OK;
            if (!internetAvailable()) {
                return STATUS_NETWORK_DISABLE;
            }
            try {
                ArrayList <DragMetal> infoStub = dailyInfoStub.getMetPrice(fromDate, toDate);
                 LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "Start update base.");
                for (DragMetal dragMetalRecord : infoStub) {
                    ContentValues _cv = dragMetalRecord.asContentValues();
                    if (cr.update(Uri.parse(METALLS_URI + "/" + dragMetalRecord.getCodeAsString()),_cv,null,null) == 0) {
                        cr.insert(Uri.parse(METALLS_URI), _cv);
                    }

                }
                 LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "Stop update base.");
            } catch (IOException e) {
                e.printStackTrace();
                LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "Error !!! : "+e.getMessage());
                res = STATUS_NOT_RESPOND;

            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                res = STATUS_NO_DATA;
            } catch (Exception e) {
                e.printStackTrace();
                res = STATUS_BAD_DATA;
                 LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "Stop update base with error: "+e.getLocalizedMessage()+"!!!");
            }


            return res;
        }

    }

    private boolean internetAvailable() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        if (netInfo == null) {
            return false;
        }
        for (NetworkInfo ni : netInfo)
        {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected()) {
                     LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "wifi connection found");
                    return true;
                }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected()) {
                     LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "mobile connection found");
                    return true;
                }
        }
         LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "Network connection not found");
        return false;
    }


}
