package ru.openitr.cbrfinfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by
 * User: oleg
 * Date: 08.04.13
 * Time: 11:46
 */

public class CurWidgetConfActivity extends Activity {

    public final static String WIDGET_PREF = "widget_pref";
    public final static String WIDGET_CURRENCY_CHARCODE = "widget_currency_charcode";
    public final static int WIDGET_CONF_DIALOG = 1;
    public final static int CUR_ITEMS_DIALOG = 2;
    private ArrayAdapter <String> curListAdapter;
    private String widgetDisplayInfo;
    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;
    final Uri CURRENCY_URI = CBInfoProvider.CURRENCY_CONTENT_URI;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null){
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID){
            finish();
        }

        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        setResult(RESULT_CANCELED, resultValue);
        //setContentView(R.layout.cur_widget_conf);
     //*************************************************

//        tv = (TextView) findViewById(R.id.widgetObject);
        ArrayList<String> curList = new ArrayList<String>();
        Cursor cursor = getContentResolver().query(CURRENCY_URI,new String[]{CbInfoDb.CUR_KEY_CHARCODE, CbInfoDb.CUR_KEY_VNAME},null, null, CbInfoDb.CUR_KEY_ORDER);
        cursor.moveToFirst();
        do {
           curList.add(cursor.getString(0)+" - "+cursor.getString(1));
        }
        while (cursor.moveToNext());
        curListAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, curList);
        widgetDisplayInfo = curList.get(0);
        cursor.close();
        showDialog(WIDGET_CONF_DIALOG);

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog resDialog = null;
        switch (id){
            case WIDGET_CONF_DIALOG:
                LayoutInflater inflater = LayoutInflater.from(this);
                View dialogView = inflater.inflate(R.layout.cur_widget_conf, null);
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.Select);
                dialogBuilder.setView(dialogView);
                resDialog = dialogBuilder.create();
                return resDialog;

            case CUR_ITEMS_DIALOG:
                AlertDialog.Builder itemsBuilder = new AlertDialog.Builder(this);
                itemsBuilder.setTitle(R.string.selectInfoItem);
                itemsBuilder.setSingleChoiceItems(curListAdapter, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView lv = ((AlertDialog) dialog).getListView();
//                        if (which == Dialog.BUTTON_POSITIVE){
                            int checkedPosition = lv.getCheckedItemPosition();
                            widgetDisplayInfo = curListAdapter.getItem(checkedPosition);
                            tv.setText(widgetDisplayInfo);

                            dialog.dismiss();
//                        }

                    }
                });
                resDialog = itemsBuilder.create();

                return resDialog;

        }

        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id){
            case WIDGET_CONF_DIALOG:
                tv = (TextView) dialog.findViewById(R.id.widgetObject);
                tv.setText(curListAdapter.getItem(0));
                break;
        }

    }

    public void onChoseButtonClick(View view){
        showDialog(CUR_ITEMS_DIALOG);
    }

    public void onClickSaveButton(View view) {
        // Берем выбранную строку из curSpinner и
        // отрезаем от нее полное наменование, оставляя только буквенный код валюты
        String _vChCode = widgetDisplayInfo.split(" - ")[0];
        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(WIDGET_CURRENCY_CHARCODE +widgetID, _vChCode);
        editor.commit();
        setResult(RESULT_OK, resultValue);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        CurrencyWidget.updateWidget(this,appWidgetManager,sp,widgetID);
        finish();
    }


}
