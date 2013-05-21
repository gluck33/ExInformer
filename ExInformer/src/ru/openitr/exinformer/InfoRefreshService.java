package ru.openitr.exinformer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
//        alarms.setInexactRepeating(alarmType, 24 * 60 * 60 * 1000, AlarmManager.INTERVAL_DAY, alarmIntent);
//        alarms.cancel(alarmIntent);
        Date newDate = new Date(intent.getLongExtra(main.PARAM_DATE, new Date().getTime()));
        onDate = newDate;
        if (main.DEBUG) Log.d(main.LOG_TAG, "Service: Service onStartCommand execute refresh task.");
        new refreshCurrencyTask().execute(newDate);
        // Запуск аларма...
        alarms.setInexactRepeating(alarmType,  System.currentTimeMillis() + 1000*60*3, AlarmManager.INTERVAL_HOUR * 3, alarmIntent);
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

        @Override
        protected Integer doInBackground(Date... params) {
            publishProgress();
            Date onDate = params[0];
//            ContentResolver cr = getContentResolver();
            CurrencyDbAdapter db = new CurrencyDbAdapter(getBaseContext());
            db.open();
            int res = OK;
            if (db.isNeedUpdate(onDate)) {
                if (main.DEBUG) Log.d(main.LOG_TAG, "Service: Info need to update.");
                if (!internetAvailable()) {
                    return STATUS_NETWORK_DISABLE;
                }
                try {
                    ArrayList<Icurrency> infoStub = new DailyInfoStub().getCursOnDate(onDate);
                    if (main.DEBUG) Log.d(main.LOG_TAG, "Service: Start update base.");
                    for (Icurrency icurrencyRecord : infoStub) {
//                        ContentValues _cv = icurrencyRecord.toContentValues();
                        if (db.updateCurrencyRow(icurrencyRecord) == 0){
                            db.insertCurrencyRow(icurrencyRecord);
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
                finally {
                    db.close();
                }
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
            Date today = new Date();
            today.setHours(0);
            today.setMinutes(0);
            today.setSeconds(0);
            int curExRange = onDate.compareTo(today);
            // Если получен курс на сегодня, то обновить информацию на виджетах
            if (datesIsEqual(onDate, new Date()))
                    sendBroadcast(widgetUpdateIntent);
            stopSelf();
        }

    }

    public boolean datesIsEqual(Date oneDate, Date twoDate){
        Calendar firstDate = Calendar.getInstance();
        firstDate.setTime((oneDate));
        Calendar secondDate = Calendar.getInstance();
        secondDate.setTime(twoDate);
        if (firstDate.get(Calendar.YEAR) != secondDate.get(Calendar.YEAR))
            return false;
        if (firstDate.get(Calendar.MONTH) != secondDate.get(Calendar.MONTH))
            return false;
        if (firstDate.get(Calendar.DAY_OF_MONTH) != secondDate.get(Calendar.DAY_OF_MONTH))
            return false;
        return true;
    }

    private boolean internetAvailable() {
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isConnectedOrConnecting();
    }

}
