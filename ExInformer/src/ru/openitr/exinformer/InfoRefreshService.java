package ru.openitr.exinformer;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.IBinder;
import ru.openitr.exinformer.DailyInfoStub;
import ru.openitr.exinformer.Icurrency;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by
 * User: Jleg Balditsyn
 * Date: 24.04.13
 * Time: 9:41
 * Сервис запрашивает информацию с сервера cbr.ru и помещает её в БД.
 */
public class InfoRefreshService extends Service{
    static final private int OK = 20;
    static final private int NOT_RESPOND = 21;
    static final private int NO_DATA = 22;
    static final private int NETWORK_DISABLE = 23;
    private Cursor mCursor;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class refreshCurrencyTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void...params){
            int res = OK;
            publishProgress();
            ContentResolver cResolver = getContentResolver();
            try {

                    if (internetAvailable()){
                        ArrayList<Icurrency> infoStub = new DailyInfoStub().getCursOnDate(onDate);
                        for (Icurrency icurrencyRecord :infoStub){
                            if (db.updateCurrencyRow(icurrencyRecord) == 0) {
                                db.insertCurrencyRow(icurrencyRecord);
                            }
                        }

                    else res = NETWORK_DISABLE;
                }

            } catch (IOException e) {
                e.printStackTrace();
                res = NOT_RESPOND;

            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                res = NO_DATA;
            } catch (Exception e){
                e.printStackTrace();
                db.close();
            }
            db.close();
            return res;
        }

        @Override
        protected void onProgressUpdate (Integer... progress){
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute (Integer result){
            super.onPostExecute(result);
            removeDialog(PROGRESS_DIALOG);
            switch (result) {
                case NOT_RESPOND:
                    removeDialog(NOT_RESPOND);
                    showDialog(NOT_RESPOND);
                case NETWORK_DISABLE:
                    showDialog(NETSETTINGS_DIALOG);
                case NO_DATA:
//                    removeDialog(DATA_DIALOG);
                    showDialog(ILLEGAL_DATA_DIALOD);
            }
            mCursor.requery();
            setDateOnTitle(onDate);
        }

    }
    public boolean internetAvailable(){
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isConnectedOrConnecting();
    }

}
