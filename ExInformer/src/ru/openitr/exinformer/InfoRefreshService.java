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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by
 * User: Oleg Balditsyn
 * Date: 24.04.13
 * Time: 9:41
 * Сервис запрашивает информацию с сервера cbr.ru и помещает её в БД.
 */
public class InfoRefreshService extends Service{
    static final private int OK = 20;
    static final private int NOT_RESPOND = 21;
    static final private int NO_DATA = 22;
    static final private int NETWORK_DISABLE = 23;
    static final String CURRENCY_URI = "content://ru.openitr.exinformer.currency/currencys";
    static final String CURRENCY_UPDATED_INTENT = "ru.openitr.exinformer.CURRENCY_UPDATED";
    static final String NOT_RESPOND_INTENT = "ru.openitr.exinformer.NOT_RESPOND";
    static final String NO_DATA_INTENT = "ru.openitr.exinformer.NO_DATA";
    static final String NETWORK_DISABLE_INTENT = "ru.openitr.exinformer.NETWORK_DISABLED";
    AlarmManager alarms;
    PendingIntent alarmIntent;
    @Override
    public void onCreate() {
        super.onCreate();
        alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        String ALARM_ACTION;
        ALARM_ACTION = InfoRefreshReciever.ACTION_REFRESH_INFO_ALARM;
        Intent intentToFire = new Intent(ALARM_ACTION);
        alarmIntent = PendingIntent.getBroadcast(this,0,intentToFire,0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
        alarms.setInexactRepeating(alarmType, 24*60*60*1000, AlarmManager.INTERVAL_DAY,alarmIntent);
        new refreshCurrencyTask().execute(new Date());
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class refreshCurrencyTask extends AsyncTask<Date, Integer, Integer> {

        @Override
        protected Integer doInBackground(Date...params){
            int res = OK;
            publishProgress();
            Date onDate = params[0];
            ContentResolver cr = getContentResolver();
            if (!internetAvailable()) {
                return NETWORK_DISABLE;
            }
            try {
                        ArrayList<Icurrency> infoStub = new DailyInfoStub().getCursOnDate(onDate);
                        for (Icurrency icurrencyRecord :infoStub){
                            ContentValues _cv = icurrencyRecord.toContentValues();
                            if (cr.update(Uri.parse(CURRENCY_URI + "/" + icurrencyRecord.getVchCode()),_cv,null,null) == 0) {
                                Uri resultUri = cr.insert(Uri.parse(CURRENCY_URI), _cv);
                            }
                        }
            } catch (IOException e) {
                e.printStackTrace();
                res = NOT_RESPOND;

            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                res = NO_DATA;
            } catch (Exception e){
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onProgressUpdate (Integer... progress){
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute (Integer result){
            super.onPostExecute(result);
            Intent resIntent = new Intent();
            switch (result) {
                case NOT_RESPOND:
                    resIntent.setAction(NOT_RESPOND_INTENT);
                case NETWORK_DISABLE:
                    resIntent.setAction(NETWORK_DISABLE_INTENT);
                case NO_DATA:
                    resIntent.setAction(NO_DATA_INTENT);
                default:
                    resIntent.setAction(CURRENCY_UPDATED_INTENT);
            }
            sendBroadcast (resIntent);
            stopSelf();
        }

    }
    public boolean internetAvailable(){
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isConnectedOrConnecting();
    }

}
