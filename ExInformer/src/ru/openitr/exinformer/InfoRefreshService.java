package ru.openitr.exinformer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by
 * User: Oleg Balditsyn
 * Date: 24.04.13
 * Time: 9:41
 * Сервис запрашивает информацию с сервера cbr.ru и помещает её в БД.
 */
public class InfoRefreshService extends Service {
    private Date onDate;

    AlarmManager alarms;
    PendingIntent alarmIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        if (main.DEBUG) Log.d(main.LOG_TAG, "Service: Service created");
        alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        String ALARM_ACTION;
        ALARM_ACTION = InfoRefreshReciever.ACTION_REFRESH_INFO_ALARM;
        Intent intentToFire = new Intent(ALARM_ACTION);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int alarmType = AlarmManager.RTC;
        Calendar nextExecuteTime = Calendar.getInstance();
        nextExecuteTime.set(Calendar.HOUR_OF_DAY, 3);
        nextExecuteTime.set(Calendar.MINUTE, 0);
        nextExecuteTime.roll(Calendar.DAY_OF_YEAR, true);
        TimeZone tz = TimeZone.getTimeZone("GMT+4");
        nextExecuteTime.setTimeZone(tz);
        long nextExecuteTimeInMills = nextExecuteTime.getTimeInMillis();
        Date newDate = new Date(intent.getLongExtra(main.PARAM_DATE, new Date().getTime()));
        onDate = newDate;
        boolean infoNeedUpdate = new CurrencyDbAdapter(getBaseContext()).isNeedUpdate(newDate);
        if (main.DEBUG) Log.d(main.LOG_TAG, "Service: Service onStartCommand execute refresh task.");
        // Если в базе информация не на текущее время, запускаем обновление данных
        if (infoNeedUpdate){
            new refreshCurrencyTask().execute(newDate);
        }
        // Запуск аларма...
        alarms.setInexactRepeating(alarmType,  nextExecuteTimeInMills, AlarmManager.INTERVAL_DAY, alarmIntent);
        //
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (main.DEBUG) Log.d(main.LOG_TAG, "Service: Destroy service.");
    }

    private class refreshCurrencyTask extends AsyncTask<Date, Integer, Integer> {
        private static final int OK = 20;
        private static final int STATUS_NETWORK_DISABLE = 30;
        private static final int STATUS_NOT_RESPOND = 40;
        private static final int STATUS_NO_DATA = 50;
        static final String CURRENCY_URI = "content://ru.openitr.exinformer.currency/currencys";
        @Override
        protected Integer doInBackground(Date... params) {
            publishProgress();
            Date onDate = params[0];
            ContentResolver cr = getContentResolver();
            int res = OK;

                if (main.DEBUG) Log.d(main.LOG_TAG, "Service: Info need to update.");
                if (!internetAvailable()) {
                    return STATUS_NETWORK_DISABLE;
                }
                try {
                    ArrayList<Icurrency> infoStub = new DailyInfoStub().getCursOnDate(onDate);
                    if (main.DEBUG) Log.d(main.LOG_TAG, "Service: Start update base.");
                    for (Icurrency icurrencyRecord : infoStub) {
                        ContentValues _cv = icurrencyRecord.toContentValues();
                        if (cr.update(Uri.parse(CURRENCY_URI + "/" + icurrencyRecord.getVchCode()),_cv,null,null) == 0) {
                            Uri resultUri = cr.insert(Uri.parse(CURRENCY_URI), _cv);
                            if (main.DEBUG) Log.d(main.LOG_TAG, "resultUri = " + resultUri.toString());
                        }
                    }
                    if (main.DEBUG) Log.d(main.LOG_TAG, "Service: Stop update base.");
                } catch (IOException e) {
                    e.printStackTrace();
                    res = STATUS_NOT_RESPOND;

                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    res = STATUS_NO_DATA;
                } catch (Exception e) {
                    e.printStackTrace();
            }

            return res;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            Intent intent = new Intent(main.INFO_REFRESH_INTENT);
            intent.putExtra(main.PARAM_STATUS, main.STATUS_BEGIN_REFRESH);
            sendBroadcast(intent);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            Intent resIntent = new Intent(main.INFO_REFRESH_INTENT);
            Intent widgetUpdateIntent = new Intent(CurrencyWidget.CURRENCY_WIDGET_UPDATE);
            switch (result) {
                case STATUS_NOT_RESPOND:
                    resIntent.putExtra(main.PARAM_STATUS, main.FIN_STATUS_NOT_RESPOND);
                case STATUS_NETWORK_DISABLE:
                    resIntent.putExtra(main.PARAM_STATUS, main.FINS_STATUS_NETWORK_DISABLE);
                case STATUS_NO_DATA:
                    resIntent.putExtra(main.PARAM_STATUS, main.FIN_STATUS_NO_DATA);
                default:
                    resIntent.putExtra(main.PARAM_STATUS, main.FIN_STATUS_OK);
            }
            sendBroadcast(resIntent);
            boolean todayInfo = !new CurrencyDbAdapter(getBaseContext()).isNeedUpdate(new Date());
               if (todayInfo)
                sendBroadcast(widgetUpdateIntent);
            stopSelf();
        }

    }

    private boolean internetAvailable() {
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isConnectedOrConnecting();
    }

}