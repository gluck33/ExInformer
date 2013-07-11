package ru.openitr.exinformer;

import android.app.*;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.*;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class main extends ListActivity {
    boolean customTitleSupported;
    static Date onDate = new Date();
    Calendar calendar = Calendar.getInstance();

     ValFromDbAdapter valFromDbAdapter;
    public static final boolean DEBUG = true;
    public static final String LOG_TAG = "CBInfo";
    public static final int STATUS_BEGIN_REFRESH = 10;
    public static final int FIN_STATUS_OK = 20;
    public static final int FIN_STATUS_NOT_RESPOND = 40;
    public static final int FIN_STATUS_NO_DATA = 50;
    public static final int FINS_STATUS_NETWORK_DISABLE = 30;
    public static final int FINS_STATUS_DATA_UPDATED = 23;
    public static final int FINS_STATUS_DATA_NOT_UPDATED = 23;


    public static final String PARAM_STATUS = "status";
    public static final String PARAM_DATE = "date";
    public static final String PARAM_FROM = "from";

    static final String INFO_REFRESH_INTENT = "ru.openitr.exinformer.INFO_UPDATE";

    static final private int DATA_DIALOG = 1;
    static final private int NETSETTINGS_DIALOG = 2;
    static final private int PROGRESS_DIALOG = 3;
    static final private int ILLEGAL_DATA_DIALOD = 4;
    static final private int NOT_RESPOND_DIALOG = 5;

    static final Uri CURRENCY_URI = Uri.parse("content://ru.openitr.exinformer.currency/currencys");
    private Cursor mCursor;
    Intent refreshServiceIntent;
    BroadcastReceiver br;

    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int from, int to) {
                    if (DEBUG)Log.d("ru.openitr.exinformer","Drop...");
                    if (from != to) {
                        DragSortListView list = getListView();
                        //String item = (String) valFromDbAdapter.getItem(from);
                        //adapter.remove(item);
                        //adapter.insert(item, to);
                        //list.moveCheckState(from, to);
                        if (DEBUG)Log.d("ru.openitr.exinformer", "Selected item is " + list.getCheckedItemPosition());
                    }
                }
            };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(LOG_TAG, "onCreate");
        customTitleSupported = Build.VERSION.SDK_INT >= 11 ? false : requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        onDate.setHours(0);
        onDate.setMinutes(0);
        onDate.setSeconds(0);
        refreshServiceIntent = new Intent(this, InfoRefreshService.class);
        mCursor = managedQuery(CURRENCY_URI, CurrencyDbAdapter.ALL_COLUMNS, null, null, null);
        startManagingCursor(mCursor);
        br = new MainActivityBroadcastReceiever();
        try {
//            listView.addHeaderView(getLayoutInflater().inflate(R.layout.currencyheader,null));
            final String[] from = CurrencyDbAdapter.ALL_VISIBLE_COLUMNS;
            final int [] to = {R.id.drag_handle, R.id.vChСodeView, R.id.vCursView, R.id.vNameView};
            //Адаптер к листу
            valFromDbAdapter = new ValFromDbAdapter(this,R.layout.currencylayuot, mCursor, from, to);
            setListAdapter(valFromDbAdapter);
            DragSortListView listView = getListView();
            listView.setDropListener(onDrop);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            getInfo(onDate);
            //Титл бар
            customTitleBar(getText(R.string.app_name).toString());
            setInfoDateToTitle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public DragSortListView getListView(){
        return (DragSortListView) super.getListView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intF = new IntentFilter(INFO_REFRESH_INTENT);
        registerReceiver(br, intF);


    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(br);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (DEBUG) Log.d(LOG_TAG, "onDestroy");
    }

    private OnDateSetListener cDateSetListener = new OnDateSetListener() {
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            Date newDate = new Date(year - 1900, month, day);
            if (!newDate.equals(onDate)){
                onDate = newDate;
                getInfo(newDate);
            }
        }

    };



    @Override
    public Dialog onCreateDialog (int id){
        switch (id){

            case (DATA_DIALOG) :
                calendar.setTime(onDate);
                DatePickerDialog dpd;
                dpd = new DatePickerDialog (this, cDateSetListener, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
                return  dpd;

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
                        getInfo(onDate);
                    }
                });
                return notRespondDlg.create();
        }
        return null; //super.onCreateDialog(id);
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

    public void setInfoDateToTitle(){
        Cursor cursor = (getContentResolver().query(CURRENCY_URI, new String[]{CurrencyDbAdapter.KEY_DATE}, null, null, null));
        if (cursor.moveToFirst())
            setDateOnTitle(new Date(cursor.getLong(0)));
    }

    private boolean internetAvailable(){
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isConnectedOrConnecting();
    }
    private void goToNetsettings(){
        Intent netSettings = new Intent("android.settings.WIRELESS_SETTINGS");
        startActivity(netSettings);
        getInfo(onDate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (!customTitleSupported){
            MenuItem dataSetItem = menu.findItem(R.id.setDataItem);
            dataSetItem.setIcon(R.drawable.holo_dark_device_access_data_usage);
        }
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

     private void getInfo(Date newDate){
        onDate = newDate;
//        boolean isNeedUpdate = db.isNeedUpdate(onDate);
        refreshServiceIntent.putExtra(PARAM_DATE,newDate.getTime());
//        if (isNeedUpdate){
            startService(refreshServiceIntent);
//        }
    }



     private class MainActivityBroadcastReceiever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(INFO_REFRESH_INTENT)){
                int status = intent.getIntExtra(PARAM_STATUS,0);
                if (DEBUG) LogSystem.logInFile(LOG_TAG, "main: (OnRecieve) Result of service run status: " + status);
                switch (status){
                    case STATUS_BEGIN_REFRESH:
                        Log.d(LOG_TAG,"Main: Begin updating info.");
                        showDialog(PROGRESS_DIALOG);
                        break;
                    case FIN_STATUS_NO_DATA:
                        Log.d(LOG_TAG, "No data receive.");

                        break;
                    case FIN_STATUS_NOT_RESPOND:
                        Log.d(LOG_TAG, "Server not respond.");
                        removeDialog(NOT_RESPOND_DIALOG);
                        showDialog(NOT_RESPOND_DIALOG);
                        break;
                    case FINS_STATUS_NETWORK_DISABLE:
                        Log.d(LOG_TAG, "Network disabled.");
                        showDialog(NETSETTINGS_DIALOG);
                        break;
                    default:
                        Log.d(LOG_TAG, "Refreshing OK.");
                        mCursor.requery();
                        setInfoDateToTitle();
                        removeDialog(PROGRESS_DIALOG);
                        break;
                }
            }

        }
    }

}
