package ru.openitr.cbrfinfo;

import android.app.*;
import android.content.*;
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
public class InfoRefreshService extends Service {

    AlarmManager alarms;
    PendingIntent alarmIntent;
    protected Calendar nextExecuteTime = Calendar.getInstance();
    protected long nextExecuteTimeInMills;
    private boolean autoupdate = false;
    private boolean lastInfo = false;
    private boolean onlySetAlarm;
    private Notification newExchangeRateNotification;
    NotificationManager notificationManager;
    SharedPreferences sharedPreferences;
    public static final int NOTIFICATION_ID = 1;
    long lastSavedDateOfExchange;
    boolean soundNotification;
    int updateInterval;
    boolean fromActivity;
    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nextExecuteTimeInMills = 0;
         LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, "Service: Service created");
        Context context = getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        autoupdate = sharedPreferences.getBoolean("PREF_AUTO_UPDATE", true);
        int hourOfRefresh = sharedPreferences.getInt("PREF_UPDITE_TIME.hour", 13);
        int minuteOfRefresh = sharedPreferences.getInt("PREF_UPDITE_TIME.minute", 0);
        soundNotification = sharedPreferences.getBoolean("PREF_SOUND_NOTIFY", true);
        lastSavedDateOfExchange = sharedPreferences.getLong("PREF_LAST_DATE",0);
        updateInterval = Integer.parseInt(sharedPreferences.getString ("PREF_UPDATE_FREQ","30"));
        updateInterval = updateInterval * 1000 * 60;
         LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, "Service: Saved last date: "+ new Date(lastSavedDateOfExchange).toLocaleString());
        if (autoupdate) {
            alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            String ALARM_ACTION;
            ALARM_ACTION = InfoRefreshReciever.ACTION_REFRESH_INFO_ALARM;
            Intent intentToFire = new Intent(ALARM_ACTION);
            alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, PendingIntent.FLAG_UPDATE_CURRENT);
            nextExecuteTime.set(Calendar.HOUR_OF_DAY, hourOfRefresh);
            nextExecuteTime.set(Calendar.MINUTE, minuteOfRefresh);
            nextExecuteTimeInMills = nextExecuteTime.getTimeInMillis();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        lastInfo = false;
        Calendar onDate = Calendar.getInstance();
        Long dateFromExtraParam = intent.getLongExtra(CurrencyInfoFragment.PARAM_DATE,0);
        onDate.setTimeInMillis(dateFromExtraParam);
        onlySetAlarm = intent.getBooleanExtra(CurrencyInfoFragment.PARAM_ONLY_SET_ALARM, false);
        fromActivity = intent.getBooleanExtra(CurrencyInfoFragment.PARAM_FROM_ACTIVITY, false);
        LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, "Service: Service onStartCommand execute refresh task.");
        new refreshCurrencyTask().execute(onDate);
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
         LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, "Service: Destroy service.");
    }

    private class refreshCurrencyTask extends AsyncTask<Calendar, Integer, Integer> {
        private static final int OK = 20;
        private static final int STATUS_NETWORK_DISABLE = 30;
        private static final int STATUS_NOT_RESPOND = 40;
        private static final int STATUS_NO_DATA = 50;
        private static final int STATUS_NOT_FRESH_DATA = 60;
        private static final int STATUS_BAD_DATA = 70;
        SharedPreferences.Editor editor;
        static final String CURRENCY_URI = "content://ru.openitr.cbrfinfo.currency/currencys";
        boolean startFromNulldate = false;
        Calendar onDate;

        @Override
        protected Integer doInBackground(Calendar... params) {
            editor = sharedPreferences.edit();
            DailyInfoStub lastDateOnServer = new DailyInfoStub();
            if (onlySetAlarm){
                 LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + ": Only need update alarmSet");
                return OK;
            }
            onDate = Calendar.getInstance();
            if ((onDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY | onDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) && !fromActivity)
                return OK;
            if (params[0].getTimeInMillis() == 0 ){
                startFromNulldate = true;
                try {
                    onDate = lastDateOnServer.getLatestDate();
                    editor.putLong("PREF_LAST_DATE", onDate.getTimeInMillis());
                    editor.commit();
                    if (onDate.getTimeInMillis() > lastSavedDateOfExchange) {
                        lastInfo = true;
                         LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG,this.getClass().getSimpleName() + " lastInfo is set to true.");
                    }
                    
                        LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " : onDate = "+ new SimpleDateFormat("dd.MM.yy HH:mm:ss").format(onDate.getTime())+". getLastDate =" + onDate.getTime().toLocaleString());
                } catch (IOException e){
                    if (!internetAvailable()){
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
            boolean infoNeedUpdate = new CurrencyDbAdapter(getBaseContext()).isNeedUpdate(onDate);
            if (infoNeedUpdate ){
                 LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " : Data need to update. newDate = " + onDate.getTime().toString() + " onDate = " + onDate.getTime().toString());
                publishProgress();
                return getCursOnDate(onDate);
            }
            if (!ExtraCalendar.isFuture(onDate)) return STATUS_NOT_FRESH_DATA;
            return OK;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            Intent intent = new Intent(CurrencyInfoFragment.INFO_REFRESH_INTENT);
            intent.putExtra(MainActivity.PARAM_STATUS, CurrencyInfoFragment.STATUS_BEGIN_REFRESH);
            sendBroadcast(intent);
        }

        @Override
        protected void onPostExecute(Integer result) {
            
                LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " : (onPostExecute) Result of service: " + result);
            int alarmType = AlarmManager.RTC;
            super.onPostExecute(result);
            Intent resIntent = new Intent(CurrencyInfoFragment.INFO_REFRESH_INTENT);
            Intent widgetUpdateIntent = new Intent(CurrencyWidget.CURRENCY_WIDGET_UPDATE);
            switch (result) {
                case STATUS_NOT_RESPOND:
                    resIntent.putExtra(MainActivity.PARAM_STATUS, CurrencyInfoFragment.FIN_STATUS_NOT_RESPOND);
                    nextExecuteTimeInMills = System.currentTimeMillis() + updateInterval;//AlarmManager.INTERVAL_FIFTEEN_MINUTES;
                    break;
                case STATUS_NETWORK_DISABLE:
                    resIntent.putExtra(MainActivity.PARAM_STATUS, CurrencyInfoFragment.FINS_STATUS_NETWORK_DISABLE);
                    nextExecuteTimeInMills = System.currentTimeMillis() + updateInterval;//AlarmManager.INTERVAL_HOUR;
                    break;
                case STATUS_NO_DATA:
                    resIntent.putExtra(MainActivity.PARAM_STATUS, CurrencyInfoFragment.FIN_STATUS_NO_DATA);
                    nextExecuteTimeInMills = System.currentTimeMillis() + updateInterval;//AlarmManager.INTERVAL_FIFTEEN_MINUTES;
                    break;
                case STATUS_NOT_FRESH_DATA:
                    resIntent.putExtra(MainActivity.PARAM_STATUS, CurrencyInfoFragment.FIN_STATUS_OK);
                    if ((System.currentTimeMillis() - nextExecuteTime.getTimeInMillis()) < (AlarmManager.INTERVAL_HOUR * 4))
                        nextExecuteTimeInMills = System.currentTimeMillis() + updateInterval;
                    else {
                        nextExecuteTime.roll(Calendar.DAY_OF_YEAR,true);
                        
                            LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + ":  End of today refreshing. Next update is tomorrow.");

                    }
                    
                        LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + ":nextExecuteTime = "+ new Date(nextExecuteTimeInMills).toLocaleString());
                    break;
                case STATUS_BAD_DATA:
                    resIntent.putExtra(MainActivity.PARAM_STATUS, CurrencyInfoFragment.FIN_STATUS_NO_DATA);
                    nextExecuteTimeInMills = System.currentTimeMillis() + updateInterval;
                    lastInfo = false;
                    break;
                default:
                    resIntent.putExtra(MainActivity.PARAM_STATUS, CurrencyInfoFragment.FIN_STATUS_OK);
                    if (nextExecuteTime.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
                        nextExecuteTime.roll(Calendar.DAY_OF_YEAR,true);
                    nextExecuteTimeInMills = nextExecuteTime.getTimeInMillis();
            }
            if (autoupdate) {
                //nextExecuteTimeInMills = System.currentTimeMillis()+1000*60*10;
                if (nextExecuteTimeInMills < System.currentTimeMillis())
                    nextExecuteTimeInMills = nextExecuteTimeInMills + AlarmManager.INTERVAL_DAY;
                alarms.set(alarmType, nextExecuteTimeInMills, alarmIntent);
                
                    LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + ": Alarm is set to " + new Date(nextExecuteTimeInMills).toLocaleString());
            }

            else if (alarms != null) alarms.cancel(alarmIntent);

            sendBroadcast(resIntent);
            if (lastInfo & result == OK){
                 LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " : lastInfo = " + lastInfo);
                widgetUpdateIntent.putExtra("CURS_TIME",Calendar.getInstance().getTimeInMillis());
                sendBroadcast(widgetUpdateIntent);
                notifyNewExchange(onDate);
            }

//            if (ExtraCalendar.isFuture(onDate)){
//                sendBroadcast(widgetUpdateIntent);
//                notifyNewExchange(onDate);
//            }

//            notifyNewExchange(onDate);
            stopSelf();
        }

        private int getCursOnDate(Calendar onDate) {
            ContentResolver cr = getContentResolver();
            DailyInfoStub dailyInfoStub = new DailyInfoStub();
            int res = OK;
             LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " : Info need to update.");
            if (!internetAvailable()) {
                return STATUS_NETWORK_DISABLE;
            }
            if (ExtraCalendar.isToday(onDate) && startFromNulldate) res = STATUS_NOT_FRESH_DATA;
            try {
                ArrayList <Icurrency> infoStub = dailyInfoStub.getCursOnDate(onDate);
                 LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " : Start update base.");
                for (Icurrency icurrencyRecord : infoStub) {
                    ContentValues _cv = icurrencyRecord.toContentValues();
                    if (cr.update(Uri.parse(CURRENCY_URI + "/" + icurrencyRecord.getVchCode()),_cv,null,null) == 0) {
                        cr.insert(Uri.parse(CURRENCY_URI), _cv);
                    }

                }
                 LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " :Stop update base.");
            } catch (IOException e) {
                e.printStackTrace();
                res = STATUS_NOT_RESPOND;

            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                res = STATUS_NO_DATA;
            } catch (Exception e) {
                e.printStackTrace();
                res = STATUS_BAD_DATA;
                 LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " :Stop update base with error: "+e.getLocalizedMessage()+"!!!");
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
                     LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " : wifi connection found");
                    return true;
                }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected()) {
                     LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " : mobile connection found");
                    return true;
                }
        }
         LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " : Network connection not found");
        return false;
    }

    public void setAlarm(long executeTime){
        AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        String ALARM_ACTION;
        ALARM_ACTION = InfoRefreshReciever.ACTION_REFRESH_INFO_ALARM;
        Intent intentToFire = new Intent(ALARM_ACTION);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, PendingIntent.FLAG_UPDATE_CURRENT);


    }

}
