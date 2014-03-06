package ru.openitr.cbrfinfo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by oleg on 25.11.13.
 */

public class CurInfoRefreshService extends InfoRefreshService{
    void readPreferencesFromFile(SharedPreferences sharedPreferences) {
        super.readPreferencesFromFile(sharedPreferences);
        autoupdate = sharedPreferences.getBoolean("PREF_AUTO_UPDATE", true);
        hourOfRefresh = sharedPreferences.getInt("PREF_UPDITE_TIME.hour", 13);
        minuteOfRefresh = sharedPreferences.getInt("PREF_UPDITE_TIME.minute", 0);
        lastSavedDateOfExchange = sharedPreferences.getLong("PREF_LAST_DATE",0);
        updateInterval = Integer.parseInt(sharedPreferences.getString ("PREF_UPDATE_FREQ","30"));
    }


    void resetPreferences(Context mContext, Calendar onDate) {
        if (Icurrency.isNeedUpdate(onDate, mContext))
            reschedule(alarms, updateInterval);
        else
            reschedule(alarms, hourOfRefresh, minuteOfRefresh);
    }

    String getTickerText(){ return getString(R.string.exchange_rate_change);}

    String setAlarmAction() {
        return CurInfoRefreshReciever.ACTION_REFRESH_INFO_ALARM;
    }

    void startTask(Calendar onDate) {
        new RefreshCurrencyInfoTask().execute(onDate);
    }

    private class RefreshCurrencyInfoTask extends RefreshInfoTask {

        Calendar getLastDateOfInfoOnServer() throws IOException {
            Calendar result;
            try {
                result = new DailyInfoStub().getLatestCurrencyDateFromServer();
            } catch (Exception e) {
                e.printStackTrace();
                result = null;
            }
            return result;
        }

        void putLastDateToPrefs(SharedPreferences sharedPreferences, long lastDateMillis) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("PREF_LAST_DATE", lastDateMillis);
            editor.commit();
        }

        boolean infoNeedUpdate(Calendar onDate) {
            return Icurrency.isNeedUpdate(onDate, mContext);
        }

        int updateInfo(Calendar onDate) {
            ContentResolver cr = getContentResolver();
            DailyInfoStub dailyInfoStub = new DailyInfoStub();
            int res = OK;
            LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " : Info need to update.");
            if (!internetAvailable((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE))) {
                return STATUS_NETWORK_DISABLE;
            }
            if (ExtraCalendar.isToday(onDate) && startFromNulldate) {
                res = STATUS_NOT_FRESH_DATA;
                LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG,                        this.getClass().getSimpleName() + "Not fresh data = "
                                + (ExtraCalendar.isToday(onDate) && startFromNulldate));
            }
            try {
                ArrayList<Icurrency> infoStub = dailyInfoStub.getCursOnDate(onDate);
                LogSystem.logInFile(CurrencyInfoFragment.LOG_TAG, this.getClass().getSimpleName() + " : Start update base.");
                for (Icurrency icurrencyRecord : infoStub) {
                    ContentValues _cv = icurrencyRecord.asContentValues();
                    if (cr.update(Uri.parse(CBInfoProvider.CURRENCY_CONTENT_URI + "/" + icurrencyRecord.getVchCode()),_cv,null,null) == 0) {
                        cr.insert(CBInfoProvider.CURRENCY_CONTENT_URI, _cv);
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

}
