package ru.openitr.exinformer;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

/**
 * Created by
 * User: oleg
 * Date: 08.04.13
 * Time: 11:46
 */

public class CurWidgetConfActivity extends Activity{

    public final static String WIDGET_PREF = "widget_pref";
    public final static String WIDGET_CURRENCY_CHARCODE = "widget_currency_charcode";

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
        setContentView(R.layout.cur_widget_conf);
     //*************************************************


        ArrayList<String> curList = new ArrayList<String>();
        Cursor cursor = getContentResolver().query(CURRENCY_URI,new String[]{CurrencyDbAdapter.KEY_CHARCODE,CurrencyDbAdapter.KEY_VNAME},null, null,null);
        cursor.moveToFirst();
        while (cursor.moveToNext()){
           curList.add(cursor.getString(0)+" - "+cursor.getString(1));
        }
        ArrayAdapter<String> curListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, curList);
        curListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner curSpinner = (Spinner) findViewById(R.id.curSpinner);
        curSpinner.setAdapter(curListAdapter);
        curSpinner.setSelection(0);
        cursor.close();

    }

    public void onClickSaveButton(View view) {
        // Берем выбранную строку из curSpinner и
        // отрезаем от нее полное наменование, оставляя только буквенный код валюты
        String _vChCode = ((Spinner) findViewById(R.id.curSpinner)).getSelectedItem().toString().split(" - ")[0];
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
