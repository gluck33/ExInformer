package ru.openitr.exinformer;

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
    private String curCurrency;
    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;
    final Uri CURRENCY_URI = Uri.parse("content://ru.openitr.exinformer.currency/currencys");
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
        setContentView(R.xml.cur_widget_conf);
     //*************************************************


        ArrayList<String> curList = new ArrayList<String>();
        Cursor cursor = getContentResolver().query(CURRENCY_URI,new String[]{CurrencyDbAdapter.KEY_CHARCODE,CurrencyDbAdapter.KEY_VNAME},null, null,null);
        cursor.moveToFirst();
        while (cursor.moveToNext()){
           curList.add(cursor.getString(0)+" - "+cursor.getString(1));
        }
        curListAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, curList);
        cursor.close();

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return super.onCreateDialog(id);
        AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(this);

//        AlertDialog confDialog = ()
        switch (id){
            case WIDGET_CONF_DIALOG:
                LayoutInflater dialogInflater = LayoutInflater.from(this);
                View dialogView = dialogInflater.inflate(R.layout.cur_widget_conf,null);
                dialogbuilder.setTitle(R.string.Select+"...");
                dialogbuilder.setView(dialogView);
                AlertDialog confDialog = dialogbuilder.create();
                return confDialog;

            case CUR_ITEMS_DIALOG:
                dialogbuilder.setTitle(R.string.Select+"...");
                dialogbuilder.setSingleChoiceItems(curListAdapter, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView lv = ((AlertDialog) dialog).getListView();
                        curCurrency = curListAdapter.getItem(lv.getCheckedItemPosition());
                    }
                });
                break;
                return dialogbuilder.create();
            default:
                return null;

        }

    }

    public void onClickSaveButton(View view) {
        // Берем выбранную строку из curSpinner и
        // отрезаем от нее полное наменование, оставляя только буквенный код валюты
        String _vChCode = curCurrency.split(" - ")[0];
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
