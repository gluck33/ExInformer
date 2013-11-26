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
import java.text.SimpleDateFormat;
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
public abstract class InfoRefreshService extends Service {

    AlarmManager alarms;
    PendingIntent alarmIntent;
    protected Calendar nextExecuteDateTime = Calendar.getInstance();
    protected long nextExecuteTimeInMills;
    protected boolean autoupdate = false;
    private boolean lastInfo = false;
    private Notification newExchangeRateNotification;
    NotificationManager notificationManager;
    SharedPreferences sharedPreferences;
    public static final int NOTIFICATION_ID = 1;
    long lastSavedDateOfExchange;
    boolean soundNotification;
    int updateInterval;
    boolean fromActivity;
    protected Context mContext;
    protected int hourOfRefresh;
    protected int minuteOfRefresh;

    abstract void readPreferencesFromFile (SharedPreferences sharedPreferences);
    abstract void resetPreferences (Context mContext, Calendar onDate);
    abstract String setAlarmAction();
    abstract void startTask(Calendar onDate);
    /**
     * Установка задачи на запуск сервиса в следующий раз
     * @param alarmManager
     * @param nextCheckTimeDelta интервал времени в милисекундах через который нужно выполнить задание.
     */

    protected   void reschedule(AlarmManager alarmManager, long nextCheckTimeDelta){
        String ALARM_ACTION = setAlarmAction();
        Intent intentToFire = new Intent(ALARM_ACTION);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, PendingIntent.FLAG_UPDATE_CURRENT);
        nextExecuteTimeInMills = nextExecuteDateTime.getTimeInMillis();
        if (nextCheckTimeDelta <= 0 ){
            LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "reschedule: alarm cancel");
            alarmManager.cancel(alarmIntent);
            stopSelf();
        }
        else{
            long now = System.currentTimeMillis();
            alarmManager.set(AlarmManager.RTC, now + nextCheckTimeDelta, alarmIntent);
            LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "reschedule: alarm set to : " + new Date(now + nextCheckTimeDelta).toLocaleString());
            stopSelf();
        }
    }

    protected void reschedule(AlarmManager alarmManager, int hour, int minute){
        if (alarmManager == null) throw new IllegalArgumentException("alarmManager is null");
        long interval = 0;
        Calendar nextDateTime = Calendar.getInstance();
        nextDateTime.set(Calendar.HOUR_OF_DAY, hour);
        nextDateTime.set(Calendar.MINUTE, minute);
        long now = System.currentTimeMillis();
        while (nextDateTime.getTimeInMillis() <= now)
            nextDateTime.roll(Calendar.DAY_OF_YEAR, 1);
        interval = nextDateTime.getTimeInMillis() - now;
        reschedule(alarmManager, interval);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Инициализация полей
        mContext = this;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nextExecuteTimeInMills = 0;
        alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "Service: Service created");
        // Чтение настроек
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        readPreferencesFromFile(sharedPreferences);
        updateInterval = updateInterval * 1000 * 60;
        LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "Service: Saved last date: "+ new Date(lastSavedDateOfExchange).toLocaleString());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this,"Service onStartCommand execute refresh task.");
        lastInfo = false;
        Calendar onDate = Calendar.getInstance();
        Long dateFromExtraParam = intent.getLongExtra(CurrencyInfoFragment.PARAM_DATE,0);
        onDate.setTimeInMillis(dateFromExtraParam);
        Boolean onlySetAlarm = intent.getBooleanExtra(CurrencyInfoFragment.PARAM_ONLY_SET_ALARM, false);
        fromActivity = intent.getBooleanExtra(CurrencyInfoFragment.PARAM_FROM_ACTIVITY, false);
        if (onlySetAlarm) {
            resetPreferences(mContext, onDate);
            return Service.START_NOT_STICKY;
        }
        startTask(onDate);
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Посылка уведомления при появлении новых данных данных.
     * @param onDate
     */
    private void notifyNewExchange(Calendar onDate){
        if (fromActivity) return;
        int icon = R.drawable.money;
        String tickerText = getString(R.string.exchange_rate_change);
        if (newExchangeRateNotification == null)
            newExchangeRateNotification = new Notification (icon, tickerText, System.currentTimeMillis());
        Context context = getApplicationContext();
        String expandedText = getString(R.string.obtained_change_in_exchange_rates);// + "  "+ ExtraCalendar.getSimpleDateString(onDate);
        String expandedTitle = tickerText;

        Intent startActivityIntent = new Intent(InfoRefreshService.this, CurrencyInfoFragment.class);

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
         LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "Destroy service.");
    }

    /******************************************
     * Процесс обновления информации в БД.
     *
     ******************************************/

    public abstract class refreshInfoTask extends AsyncTask<Calendar, Integer, Integer> {
        protected static final int OK = 20;
        protected static final int STATUS_NETWORK_DISABLE = 30;
        protected static final int STATUS_NOT_RESPOND = 40;
        protected static final int STATUS_NO_DATA = 50;
        protected static final int STATUS_NOT_FRESH_DATA = 60;
        protected static final int STATUS_BAD_DATA = 70;
        SharedPreferences.Editor editor;
        boolean startFromNulldate = false;
        Calendar onDate;

        abstract Calendar getLastDateOfInfo() throws IOException;
        abstract void putLastDateToPrefs(SharedPreferences sharedPreferences, long lastDateMillis);
        abstract boolean infoNeedUpdate(Calendar onDate);
        abstract int updateInfo(Calendar onDate);
        @Override
        protected Integer doInBackground(Calendar... params) {
            //return OK;
            editor = sharedPreferences.edit();
            onDate = Calendar.getInstance();
            if ((onDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY | onDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) && !fromActivity)
                return OK;
            if (params[0].getTimeInMillis() == 0 ){
                startFromNulldate = true;
                try {
                    onDate = getLastDateOfInfo();
                    putLastDateToPrefs(sharedPreferences, onDate.getTimeInMillis());
                    if (onDate.getTimeInMillis() > lastSavedDateOfExchange) {
                        lastInfo = true;
                         LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG,this.getClass().getSimpleName() + " lastInfo is set to true.");
                    }

                        LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " : onDate = "+ new SimpleDateFormat("dd.MM.yy HH:mm:ss").format(onDate.getTime())+". getLastDate =" + onDate.getTime().toLocaleString());
                } catch (IOException e){
                    if (!internetAvailable((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE))){
                        return STATUS_NETWORK_DISABLE;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG,this.getClass().getSimpleName() + " Not get last date from server.");
                    return STATUS_BAD_DATA;
                }
            }
            else {
                onDate = params[0];
            }
            boolean infoNeedUpdate = infoNeedUpdate(onDate);
            if (infoNeedUpdate ){
                 LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " : Data need to update. newDate = " + onDate.getTime().toString() + " onDate = " + onDate.getTime().toString());
                publishProgress();
                return updateInfo(onDate);
            }
            if (!ExtraCalendar.isFuture(onDate)) return STATUS_NOT_FRESH_DATA;
            return OK;
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
            LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " : (onPostExecute) Result of service: " + result);
            super.onPostExecute(result);
            Intent resIntent = new Intent(MainActivity.INFO_REFRESH_INTENT);
            Intent widgetUpdateIntent = new Intent(CurrencyWidget.CURRENCY_WIDGET_UPDATE);
            switch (result) {
                case STATUS_NOT_RESPOND:
                case STATUS_NETWORK_DISABLE:
                case STATUS_NO_DATA:
                case STATUS_BAD_DATA:
                    resIntent.putExtra(MainActivity.PARAM_STATUS, result);
                    reschedule(alarms, updateInterval);
                    lastInfo = false;
                    break;
                case STATUS_NOT_FRESH_DATA:
                    resIntent.putExtra(MainActivity.PARAM_STATUS, OK);
                    if ((System.currentTimeMillis() - nextExecuteDateTime.getTimeInMillis()) < (AlarmManager.INTERVAL_HOUR * 4)){
                        if (!fromActivity)
                            reschedule(alarms, updateInterval);
                    }
                    else {
                        nextExecuteDateTime.roll(Calendar.DAY_OF_YEAR,true);
                            LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + ":  End of today refreshing. Next update is tomorrow.");
                    }
                    break;
                default:
                    resIntent.putExtra(MainActivity.PARAM_STATUS, CurrencyInfoFragment.FIN_STATUS_OK);
            }
            if (autoupdate)
                reschedule(alarms, hourOfRefresh, minuteOfRefresh);
            sendBroadcast(resIntent);
            if (lastInfo && result == OK){
                LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " : lastInfo = " + lastInfo);
                widgetUpdateIntent.putExtra("CURS_TIME",Calendar.getInstance().getTimeInMillis());
                sendBroadcast(widgetUpdateIntent);
                notifyNewExchange(onDate);
            }
            stopSelf();
        }
    }


    public static boolean internetAvailable(ConnectivityManager cm) {
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
                    LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, "wifi connection found");
                    return true;
                }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected()) {
                    LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, "mobile connection found");
                    return true;
                }
        }
        LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, "Network connection not found");
        return false;
    }

}
