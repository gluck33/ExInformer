package ru.openitr.exinformer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
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
 * Сервис запрашивает информацию с сервера cbr.ru и помещает её в БД.
 */
public class InfoRefreshService extends Service {
    private Date onDate;

    AlarmManager alarms;
    PendingIntent alarmIntent;
    protected Calendar nextExecuteTime = Calendar.getInstance();
    protected long nextExecuteTimeInMills;
    private boolean autoupdate = false;
    @Override
    public void onCreate() {
        super.onCreate();
        nextExecuteTimeInMills = 0;
        if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Service: Service created");
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        autoupdate = sharedPreferences.getBoolean("PREF_AUTO_UPDATE", false);
        int hourOfRefresh = sharedPreferences.getInt("PREF_UPDITE_TIME.hour", 0);
        int minuteOfRefresh = sharedPreferences.getInt("PREF_UPDITE_TIME.minute", 0);
        if (autoupdate) {
            alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            String ALARM_ACTION;
            ALARM_ACTION = InfoRefreshReciever.ACTION_REFRESH_INFO_ALARM;
            Intent intentToFire = new Intent(ALARM_ACTION);
            alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, PendingIntent.FLAG_UPDATE_CURRENT);
            nextExecuteTime.set(Calendar.HOUR_OF_DAY, hourOfRefresh);
            nextExecuteTime.set(Calendar.MINUTE, minuteOfRefresh);
            nextExecuteTime.roll(Calendar.DAY_OF_YEAR, true);
            nextExecuteTimeInMills = nextExecuteTime.getTimeInMillis();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Calendar onDate = Calendar.getInstance();
        Long extraParam = intent.getLongExtra(main.PARAM_DATE,0);
        onDate.setTimeInMillis(extraParam);

        boolean infoNeedUpdate = new CurrencyDbAdapter(getBaseContext()).isNeedUpdate(onDate);
        if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Service: Service onStartCommand execute refresh task.");
        // Если в базе информация не на текущее время, запускаем обновление данных
        if (infoNeedUpdate){
            if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Data need to update. newDate = " + onDate.toString() + " onDate = " + onDate.toString() + " infoNeedUpdate = "+infoNeedUpdate);
            new refreshCurrencyTask().execute(onDate);
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Service: Destroy service.");
    }

    private class refreshCurrencyTask extends AsyncTask<Calendar, Integer, Integer> {
        private static final int OK = 20;
        private static final int STATUS_NETWORK_DISABLE = 30;
        private static final int STATUS_NOT_RESPOND = 40;
        private static final int STATUS_NO_DATA = 50;
        static final String CURRENCY_URI = "content://ru.openitr.exinformer.currency/currencys";
        private DailyInfoStub dailyInfo = new DailyInfoStub();
        @Override
        protected Integer doInBackground(Calendar... params) {
            publishProgress();
            return getCursOnDate(params[0]);
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
            int alarmType = AlarmManager.RTC;
            super.onPostExecute(result);
            Intent resIntent = new Intent(main.INFO_REFRESH_INTENT);
            Intent widgetUpdateIntent = new Intent(CurrencyWidget.CURRENCY_WIDGET_UPDATE);
            switch (result) {
                case STATUS_NOT_RESPOND:
                    resIntent.putExtra(main.PARAM_STATUS, main.FIN_STATUS_NOT_RESPOND);
                    nextExecuteTimeInMills = System.currentTimeMillis() + 1000*60*60;
                    alarms.setInexactRepeating(alarmType,  nextExecuteTimeInMills, AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
                    break;
                case STATUS_NETWORK_DISABLE:
                    resIntent.putExtra(main.PARAM_STATUS, main.FINS_STATUS_NETWORK_DISABLE);
                    nextExecuteTimeInMills = System.currentTimeMillis() + 1000*60*60;
                    alarms.setInexactRepeating(alarmType,  nextExecuteTimeInMills , AlarmManager.INTERVAL_HOUR, alarmIntent);
                    break;
                case STATUS_NO_DATA:
                    resIntent.putExtra(main.PARAM_STATUS, main.FIN_STATUS_NO_DATA);
                    break;
                default:
                    resIntent.putExtra(main.PARAM_STATUS, main.FIN_STATUS_OK);
                    if (autoupdate) alarms.setInexactRepeating(alarmType,  nextExecuteTimeInMills, AlarmManager.INTERVAL_DAY, alarmIntent);
            }
            if (main.DEBUG) {
                LogSystem.logInFile(main.LOG_TAG, "Service: (onPostExecute) Result of service: " + result);
                LogSystem.logInFile(main.LOG_TAG, "Alarm is set to " + nextExecuteTime.getTime().toLocaleString());
            }
            sendBroadcast(resIntent);
            boolean todayInfo = !new CurrencyDbAdapter(getBaseContext()).isNeedUpdate(Calendar.getInstance());
               if (todayInfo){
                widgetUpdateIntent.putExtra("CURS_TIME",Calendar.getInstance().getTimeInMillis());
                sendBroadcast(widgetUpdateIntent);
               }


            stopSelf();
        }

        private int getCursOnDate(Calendar onDate){
            ContentResolver cr = getContentResolver();
            int res = OK;
            if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Service: Info need to update.");
            if (!internetAvailable()) {
                return STATUS_NETWORK_DISABLE;
            }
            try {
                ArrayList <Icurrency> infoStub = dailyInfo.getCursOnDate(onDate);
                if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Service: Start update base.");
                for (Icurrency icurrencyRecord : infoStub) {
                    ContentValues _cv = icurrencyRecord.toContentValues();
                    if (cr.update(Uri.parse(CURRENCY_URI + "/" + icurrencyRecord.getVchCode()),_cv,null,null) == 0) {
                        Uri resultUri = cr.insert(Uri.parse(CURRENCY_URI), _cv);
                        if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "resultUri = " + resultUri.toString());
                    }
                }
                if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "Service: Stop update base.");
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

    }

    private boolean internetAvailable() {
        ConnectivityManager cm =        (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
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
                    if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "test: wifi conncetion found");
                    return true;
                }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected()) {
                    if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "test: mobile conncetion found");
                    return true;
                }
        }
        if (main.DEBUG) LogSystem.logInFile(main.LOG_TAG, "test: Network conncetion not found");
        return false;
    }



}
