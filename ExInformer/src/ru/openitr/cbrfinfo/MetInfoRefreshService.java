package ru.openitr.cbrfinfo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by oleg on 26.11.13.
 */
public class MetInfoRefreshService extends InfoRefreshService {
    @Override
    void readPreferencesFromFile(SharedPreferences sharedPreferences) {
        super.readPreferencesFromFile(sharedPreferences);
        autoupdate = sharedPreferences.getBoolean("PREF_MET_AUTO_UPDATE", true);
        hourOfRefresh = sharedPreferences.getInt("PREF_MET_UPDATE_TIME.hour", 13);
        minuteOfRefresh = sharedPreferences.getInt("PREF_MET_UPDATE_TIME.minute", 0);
        lastSavedDateOfExchange = sharedPreferences.getLong("PREF_LAST_METAL_DATE", 0);
        // TODO Сделать храненин параметра в файле свойств или убрать совсем переменную updateInterval.
        //updateInterval = Integer.parseInt(sharedPreferences.getString ("PREF_UPDATE_FREQ","30"));
    }

    void resetPreferences(Context mContext, Calendar onDate) {
        if (DragMetal.isNeedUpdate(onDate, mContext))
            reschedule(alarms, updateInterval);
        else
            reschedule(alarms, hourOfRefresh, minuteOfRefresh);

    }

    String setAlarmAction() {
        return MetInfoRefreshReciever.ACTION_REFRESH_MET_INFO_ALARM;
    }

    String getTickerText(){return getString(R.string.metall_rate_change);}

    String getExpandetText(){return getString(R.string.obtained_change_in_metall_rates);}

    void startTask(Calendar onDate) {
        new RefreshMetalInfoTask().execute(onDate);
    }

    private class RefreshMetalInfoTask extends RefreshInfoTask {

        Calendar getLastDateOfInfoOnServer() throws IOException {
            return new DailyInfoStub().getLatestMetalDateFromServer();
        }

        void putLastDateToPrefs(SharedPreferences sharedPreferences, long lastDateMillis) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("PREF_LAST_METAL_DATE", lastDateMillis);
            editor.commit();
        }

        boolean infoNeedUpdate(Calendar onDate) {
            return DragMetal.isNeedUpdate(onDate, mContext);
        }

        int updateInfo(Calendar toDate) {
            ContentResolver cr = getContentResolver();
            DailyInfoStub dailyInfoStub = new DailyInfoStub();
            Calendar fromDate = Calendar.getInstance();
            fromDate.setTimeInMillis(toDate.getTimeInMillis());
            fromDate.roll(Calendar.DAY_OF_YEAR, -7);
            int res = OK;
            if (!CurInfoRefreshService.internetAvailable((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE))) {
                return STATUS_NETWORK_DISABLE;
            }
            try {
                ArrayList<DragMetal> infoStub = dailyInfoStub.getMetPrice(fromDate, toDate);
                // Получаем дату последнего элемента в ответе сервера
                Calendar lastDateInfoOnServer = infoStub.get(infoStub.size() - 1).getOnDate();
                // Если эта дата - сегодня и сервис был запущен из аларма то это значит что данных на завтра нет.
                // Т.е. свежих данных на сервере еще нет.
                LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG,this,"Last date of data is: " +  new SimpleDateFormat("dd.MM.yy HH:mm:ss").format(lastDateInfoOnServer.getTime()));
                if (startFromNulldate & ExtraCalendar.isToday(lastDateInfoOnServer)& !fromActivity)
                    return STATUS_NOT_FRESH_DATA;
                LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "Start update base.");
                    for (DragMetal dragMetalRecord : infoStub) {
                        ContentValues _cv = dragMetalRecord.asContentValues();
                        String updateURI = CBInfoProvider.METAL_CONTENT_URI + "/" + dragMetalRecord.getCodeAsString();
                        if (cr.update(Uri.parse(updateURI), _cv, null, null) == 0) {
                            cr.insert(CBInfoProvider.METAL_CONTENT_URI, _cv);
                        }
                    }
                LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "Stop update base.");
            } catch (IOException e) {
                e.printStackTrace();
                LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "Error !!! : "+e.getMessage());
                res = STATUS_NOT_RESPOND;
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "Error !!! : "+e.getMessage());
                res = STATUS_NO_DATA;

            } catch (Exception e) {
                e.printStackTrace();
                res = STATUS_BAD_DATA;
                LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this, "Stop update base with error: "+e.getLocalizedMessage()+"!!!");
            }

            return res;

        }
    }

}
