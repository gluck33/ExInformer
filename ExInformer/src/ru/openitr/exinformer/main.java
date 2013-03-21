package ru.openitr.exinformer;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import ru.openitr.exinformerlib.Currency;
import ru.openitr.exinformerlib.CurrencyDbAdapter;
import ru.openitr.exinformerlib.DailyInfoStub;
import ru.openitr.exinformerlib.ValFromDbAdapter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class main extends ListActivity {
    Cursor mCursor;
    boolean customTitleSupported;
    static Date onDate = new Date();
    Calendar calendar = Calendar.getInstance();
    ValFromDbAdapter valFromDbAdapter;
    CurrencyDbAdapter db;
    static final private int DATA_DIALOG = 1;
    static final private int NETSETTINGS_DIALOG = 2;
    static final private int PROGRESS_DIALOG = 3;
    static final private int ILLEGAL_DATA_DIALOD = 4;
    static final private int NOT_RESPOND_DIALOG = 5;

    static final private int OK = 20;
    static final private int NOT_RESPOND = 21;
    static final private int NO_DATA = 22;
    static final private int NETWORK_DISABLE = 23;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customTitleSupported = Build.VERSION.SDK_INT >= 11 ? false : requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        onDate.setHours(0);
        onDate.setMinutes(0);
        onDate.setSeconds(0);
        db = new CurrencyDbAdapter(this);
        getExchange(onDate, (savedInstanceState == null ? true : false));
        mCursor = db.getAllCurRowsCursor();
        try {
            ListView listView = getListView();
            listView.addHeaderView(getLayoutInflater().inflate(R.layout.currencyheader,null));
            final String[] from = CurrencyDbAdapter.ALL_VISIBLE_COLUMNS;
            final int [] to = {R.id.flag_image, R.id.vChСodeView, R.id.vNomView, R.id.vCursView, R.id.vNameView};
            //Адаптер к листу
             valFromDbAdapter = new ValFromDbAdapter(this,R.layout.currencylayuot, mCursor, from, to);
            setListAdapter(valFromDbAdapter);
            //Титл бар

            customTitleBar(getText(R.string.app_name).toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

/*    @Override
    public void onResume(){
        super.onResume();
    }
*/
    @Override
    public void onDestroy(){
        db.close();
    }

    private DatePickerDialog.OnDateSetListener cDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            getExchange(new Date(year-1900, month, day), false);
        }
    };



    @Override
    public Dialog onCreateDialog (int id){
        switch (id){

            case (DATA_DIALOG) :
                calendar.setTime(onDate);
                return  new DatePickerDialog (this, cDateSetListener, calendar.get(Calendar.YEAR),
                                              calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

            case (NETSETTINGS_DIALOG) :
                AlertDialog.Builder netSettingsDialog = new AlertDialog.Builder(this);
                netSettingsDialog.setTitle(R.string.app_name);
                netSettingsDialog.setMessage(R.string.netSettingsDlgMessage);
                netSettingsDialog.setCancelable(false);
                netSettingsDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i){
                        dialogInterface.cancel();
                    }
                });
                netSettingsDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        goToNetsettings();
                    }
                });
                return netSettingsDialog.create();

            case (PROGRESS_DIALOG) :
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(getText(R.string.loading));
                return progressDialog;

            case (ILLEGAL_DATA_DIALOD):
                AlertDialog.Builder msgDlg = new AlertDialog.Builder(this);
                msgDlg.setTitle(R.string.futureTitle);
                msgDlg.setMessage(R.string.futureMsg);
                msgDlg.setCancelable(false);
                msgDlg.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onDate = new Date();
                        dialogInterface.dismiss();
                    }
                });
                return msgDlg.create();
            case (NOT_RESPOND_DIALOG):
                AlertDialog.Builder notRespondDlg = new AlertDialog.Builder(this);
                notRespondDlg.setTitle(R.string.notRespondDlgTitle);
                notRespondDlg.setMessage(R.string.notRespondDlgMsg);
                notRespondDlg.setCancelable(true);
                notRespondDlg.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                    }
                });
                notRespondDlg.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getExchange(onDate, false);
                    }
                });
                return notRespondDlg.create();
        }
        return null;
    }


    /**
       Title bar для вывода даты курса.
     */
    private void customTitleBar(String left) {
        if (customTitleSupported) {
             getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                    R.layout.apptitle);
            TextView titleLeft = (TextView) findViewById(R.id.titleLeft);
            titleLeft.setText(left);
            setDateOnTitle(onDate);
        }
    }

    private void setDateOnTitle(Date onDate){
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String stringDate = sdf.format(onDate);
        if (customTitleSupported) {
            TextView titleTvRight = (TextView) findViewById(R.id.titleRight);
            titleTvRight.setText(getText(R.string.appTitleDatePrefix).toString() + ": "+stringDate);
        }
        else {
            ActionBar bar = getActionBar();
            bar.setSubtitle(getString(R.string.appTitleDatePrefix)+ ": " + stringDate);
        }

    }

    private boolean internetAvailable(){
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isConnectedOrConnecting();
    }
    private void goToNetsettings(){
        Intent netSettings = new Intent("android.settings.WIRELESS_SETTINGS");
        startActivity(netSettings);
        getExchange(onDate,false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
                case (R.id.setDataItem):
                    showDialog(DATA_DIALOG);
            }
        return true;
    }


    private void getExchange(Date newDate, boolean newInstance){
        onDate = newDate;
        if (newInstance || db.needUpdate(onDate))
            try {
                new refreshCurrencyTask().execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }


    private class refreshCurrencyTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void...params){
            int res = OK;
                publishProgress();
                try {
                    if (db.needUpdate(onDate)) {
                        if (internetAvailable()){
                            ArrayList<Currency> infoStub = new DailyInfoStub().getCursOnDate(onDate);
                            //db.deleteAllRows();
                            for (Currency currencyRecord:infoStub){
                                if (db.updateCurrencyRow(currencyRecord) == 0) {
                                    db.insertCurrencyRow(currencyRecord);
                                }
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
                }

            return res;
        }

        @Override
        protected void onProgressUpdate (Integer... progress){
            super.onProgressUpdate(progress);
            showDialog(PROGRESS_DIALOG);
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
}
