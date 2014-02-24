package ru.openitr.cbrfinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

        import android.support.v4.app.DialogFragment;
/**
 * Created by
 * User: oleg
 * Date: 08.04.13
 * Time: 11:46
 */

public class CurWidgetConfActivity extends FragmentActivity {

    public  final static String WIDGET_PREF = "widget_pref";
    public final static String WIDGET_CURRENCY_CHARCODE = "widget_currency_charcode";
    public final static String WIDGET_METAL_CODE = "widget_metal_code";
    public final static String WIDGET_INFO_TYPE = "widget_info_type";
    public final static int INFO_TYPE_CURRENCY = 0;
    public final static int INFO_TYPE_METAL = 1;
    public final static int INFO_TYPE_NEWS = 2;
    private ArrayAdapter <String> curListAdapter;
    private String widgetDisplayInfo;
    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;
    final Uri CURRENCY_URI = CBInfoProvider.CURRENCY_CONTENT_URI;
    protected SelectListAlertDialog selectInfoTypeDialog;
    protected SelectListAlertDialog selectCurrencyDialog;
    protected SelectListAlertDialog selectMetalDialog;
    protected ArrayList<String> pageList = new ArrayList<String>();
    protected ArrayList<String> metalsList = new ArrayList<String>();
    protected ArrayList<String> curList = new ArrayList<String>();
    protected ConfigDialog dialogView;


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
        //setContentView(R.layout.cur_widget_conf);
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        setResult(RESULT_CANCELED, resultValue);

        // АrrayList для выбора типа информации

        String[] pageNames = getResources().getStringArray(R.array.page_titles);
        for (int i = 0; i<= pageNames.length - 1; i ++)
            pageList.add(pageNames[i]);

        // АrrayList со списком металлов.

        String[] metalNames = getResources().getStringArray(R.array.metall_names);
        for (int i = 0; i<= metalNames.length - 1; i ++)
            metalsList.add(metalNames[i]);

        // АrrayList со списком валют.

        Cursor cursor = getContentResolver().query(CURRENCY_URI,new String[]{CbInfoDb.CUR_KEY_CHARCODE, CbInfoDb.CUR_KEY_VNAME},null, null, CbInfoDb.CUR_KEY_ORDER);
        cursor.moveToFirst();
        do {
           curList.add(cursor.getString(0) + " - " + cursor.getString(1));
        }
        while (cursor.moveToNext());

        widgetDisplayInfo = curList.get(0);
        cursor.close();

        // Создание диалоговых фрагментов

        //

        // Диалог выбора валюты.
        selectCurrencyDialog = SelectListAlertDialog.newInstance(curList, "currencys", new CurrencyListChoiceListener());
        //Диалог выбора металлов.
        selectMetalDialog = SelectListAlertDialog.newInstance(metalsList, "metalls", new MetalListChoiceListener());
        // Выбор типа информации
        selectInfoTypeDialog = SelectListAlertDialog.newInstance(pageList, getText(R.string.selectInfoItem).toString(), new PageListChoseListener());
        selectInfoTypeDialog.show(getSupportFragmentManager(), "page");
        // Конец АrrayList для со списком валют.
//        showDialog(WIDGET_CONF_DIALOG);

    }
//
//    public static void setChoicedText (String text){
//        TextView tv = (TextView) findViewById(R.id.widgetObject);
//        tv.setText(text);
//    }


    void showChoiceCurrencyDialog(){
        selectCurrencyDialog.show(getSupportFragmentManager(), "currencys");
    }

    void showChoiceMetallDialog(){
        selectMetalDialog.show(getSupportFragmentManager(), "metalls");
    }

    void showConfigDialog(String chosenText, int infoType){
        dialogView = ConfigDialog.newInstance(chosenText, infoType, new OnClickSaveButton(infoType));
        dialogView.setOnChoiceListener(new OnClickChoiceButton(infoType));
        dialogView.show(getSupportFragmentManager(), "widgetconfig");
    }

    /**
     * Обработка выбора из списка страниц. Т.е. вида информации (Валюты, металлы и т.д.)
     */

    private class PageListChoseListener implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            ListView lv = ((AlertDialog) dialogInterface).getListView();
            int checkedPosition = lv.getCheckedItemPosition();
            switch (checkedPosition){
                case INFO_TYPE_CURRENCY: selectCurrencyDialog.show(getSupportFragmentManager(), "currencys");
                        break;
                case INFO_TYPE_METAL: selectMetalDialog.show(getSupportFragmentManager(), "metals");
                        break;
            }
            dialogInterface.dismiss();
        }
    }

    /**
     * Обработка выбора из списка валют.
     */

    private class CurrencyListChoiceListener implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            ListView lv = ((AlertDialog) dialogInterface).getListView();
            int checkedPosition = lv.getCheckedItemPosition();
            widgetDisplayInfo = curList.get(checkedPosition);
            dialogInterface.dismiss();
            showConfigDialog(widgetDisplayInfo, INFO_TYPE_CURRENCY);
            }

        }
    /**
     * Обработка выбора из списка металлов.
     */

    private class MetalListChoiceListener implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            ListView lv = ((AlertDialog) dialogInterface).getListView();
            int checkedPosition = lv.getCheckedItemPosition();
            widgetDisplayInfo = metalsList.get(checkedPosition);
            dialogInterface.dismiss();
            showConfigDialog(widgetDisplayInfo, INFO_TYPE_METAL);

        }
    }

    /**
     * Обработка нажатия кнопки сохранить. (Сохранение того, что будет отражаться в виджете)
     */

    public class  OnClickSaveButton implements View.OnClickListener {
        int infoType;

        public OnClickSaveButton (int infoType){
            this.infoType = infoType;
        }

        @Override
        public void onClick(View view) {
            SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            switch (this.infoType){
                case INFO_TYPE_CURRENCY:
                    String _vChCode = widgetDisplayInfo.split(" - ")[0];
                    editor.putString(WIDGET_CURRENCY_CHARCODE + widgetID, _vChCode);
                    editor.putString(WIDGET_INFO_TYPE + widgetID, String.valueOf(INFO_TYPE_CURRENCY));
                    editor.commit();
                    break;
                case INFO_TYPE_METAL:
                    String metalCode = String.valueOf(metalsList.indexOf(widgetDisplayInfo)+1);
                    editor.putString(WIDGET_METAL_CODE + widgetID, metalCode);
                    editor.putString(WIDGET_INFO_TYPE + widgetID, String.valueOf(INFO_TYPE_METAL));
                    editor.commit();
                    break;
            }
            setResult(RESULT_OK, resultValue);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(dialogView.getActivity());
            InfoWidget.updateWidget(dialogView.getActivity(), appWidgetManager, sp, widgetID);
            finish();

        }
    }

    /**
     * Обработка нажатия кнопки выбора из списка "\/" в диалоге конфигурации виджета
     */


    public class OnClickChoiceButton implements View.OnClickListener{

        private int infType;

        public OnClickChoiceButton(int infType) {
            this.infType = infType;
        }

        @Override
        public void onClick(View view) {
           switch (infType){
               case INFO_TYPE_CURRENCY:
                   showChoiceCurrencyDialog();
                   break;
               case INFO_TYPE_METAL:
                   showChoiceMetallDialog();
                   break;
           }
        }
    }
    /**
     *  Класс для вывода диалога одиночного выбора
     */

    public static class SelectListAlertDialog extends DialogFragment{
        ArrayList<String> selectArgs = new ArrayList<String>();
        ListAdapter selectListAdapter;
        DialogInterface.OnClickListener selectChoiceListener;

        /**
         * Создание экземпляра класса
         * @param selectionList - список пунктов в списке выбора
         * @param title - заголовок окна диалога
         * @param onSelectItemListener Listener обработки выбора
         * @return Экземпляр окна диалога со списком выбора.
         */

        public static SelectListAlertDialog newInstance(ArrayList<String> selectionList, String title, DialogInterface.OnClickListener onSelectItemListener){
            SelectListAlertDialog dialog = new SelectListAlertDialog(selectionList, onSelectItemListener);
            Bundle args = new Bundle();
            args.putString("title", title);
            dialog.setArguments(args);
            return dialog;
        }


        private SelectListAlertDialog(ArrayList<String> selectArgs, DialogInterface.OnClickListener onSelectItemListener){
            this.selectArgs = selectArgs;
            this.selectChoiceListener = onSelectItemListener;
        }

        public void setOnSelectItemListener(DialogInterface.OnClickListener selectChoiceListener){
            this.selectChoiceListener = selectChoiceListener;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setCancelable(false);
            setStyle(DialogFragment.STYLE_NORMAL, 0);
        }


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String title = getArguments().getString("title");
            selectListAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice, selectArgs);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(title);
            builder.setSingleChoiceItems(selectListAdapter, 0, selectChoiceListener);
            return builder.create();
        }

    }

    /**
     * Окно настройки виджета, сделанное в виде диалога.
     */

    public static class ConfigDialog extends DialogFragment{
        protected static String chosenString;
        protected static int infoType;
        protected static View.OnClickListener onSaveListener;
        protected View.OnClickListener onChoiceClickListener;

        static ConfigDialog newInstance(String chosenString, int infoType, View.OnClickListener onSaveListener){
            return new ConfigDialog(chosenString, infoType, onSaveListener);
        }

        public ConfigDialog(String chosenString, int infoType, View.OnClickListener onSaveListener) {
            this.chosenString = chosenString;
            this.infoType = infoType;
            this.onSaveListener = onSaveListener;
        }

        public void setOnChoiceListener(View.OnClickListener l){
            this.onChoiceClickListener = l;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            //setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Dialog);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.cur_widget_conf, container,false);
            TextView chosenTextView = (TextView) v.findViewById(R.id.widgetObject);
            chosenTextView.setText(chosenString);
            ImageButton choiceButton = (ImageButton) v.findViewById(R.id.imageButton);
            choiceButton.setOnClickListener(this.onChoiceClickListener);
            Button saveButton = (Button) v.findViewById(R.id.SelectButton);
            saveButton.setOnClickListener(onSaveListener);
            return v;
        }



    }


    //    @Override
//    protected Dialog onCreateDialog(int id) {
//        AlertDialog resDialog;
//        switch (id){
//            case WIDGET_CONF_DIALOG:
//                LayoutInflater inflater = LayoutInflater.from(this);
//                View dialogView = inflater.inflate(R.layout.cur_widget_conf, null);
//                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//                dialogBuilder.setTitle(R.string.Select);
//                dialogBuilder.setView(dialogView);
//                resDialog = dialogBuilder.create();
//                return resDialog;
//
//            case CUR_ITEMS_DIALOG:
//                AlertDialog.Builder itemsBuilder = new AlertDialog.Builder(this);
//                itemsBuilder.setTitle(R.string.selectInfoItem);
//                itemsBuilder.setSingleChoiceItems(curListAdapter, -1, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        ListView lv = ((AlertDialog) dialog).getListView();
////                        if (which == Dialog.BUTTON_POSITIVE){
//                            int checkedPosition = lv.getCheckedItemPosition();
//                            widgetDisplayInfo = curListAdapter.getItem(checkedPosition);
//                            tv.setText(widgetDisplayInfo);
//                            dialog.dismiss();
////                        }
//
//                    }
//                });
//                resDialog = itemsBuilder.create();
//
//                return resDialog;
//
//        }
//
//        return null;
//    }
//
//    @Override
//    protected void onPrepareDialog(int id, Dialog dialog) {
//        switch (id){
//            case WIDGET_CONF_DIALOG:
//                tv = (TextView) dialog.findViewById(R.id.widgetObject);
//                tv.setText(curListAdapter.getItem(0));
//                break;
//        }
//
//    }
//
//    public void onChoseButtonClick(View view){
//        showDialog(CUR_ITEMS_DIALOG);
//    }
//
//    public void onClickSaveButton(View view) {
//        // Берем выбранную строку из curSpinner и
//        // отрезаем от нее полное наменование, оставляя только буквенный код валюты
//        String _vChCode = widgetDisplayInfo.split(" - ")[0];
//        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit();
//        editor.putString(WIDGET_CURRENCY_CHARCODE +widgetID, _vChCode);
//        editor.commit();
//        setResult(RESULT_OK, resultValue);
//        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
//        CurrencyWidget.updateWidget(this,appWidgetManager,sp,widgetID);
//        finish();
//    }


}
