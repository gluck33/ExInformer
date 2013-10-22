package ru.openitr.cbrfinfo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by oleg on 22.10.13.
 */
public class Dialogs extends DialogFragment {
    static final public int DATA_DIALOG = 1;
    static final public int NETSETTINGS_DIALOG = 2;
    static final public int PROGRESS_DIALOG = 3;
    static final public int ILLEGAL_DATA_DIALOD = 4;
    static final public int NOT_RESPOND_DIALOG = 5;
    static Calendar onDate;




    public Dialog onCreatedialog(/*Bundle savedInstanceState, */int id, Context context){
        switch (id) {

            case (DATA_DIALOG):
                DatePickerDialog dpd;
                dpd = new DatePickerDialog(context, cDateSetListener, onDate.get(Calendar.YEAR),
                        onDate.get(Calendar.MONTH), onDate.get(Calendar.DATE));
                return dpd;

            case (NETSETTINGS_DIALOG):
                AlertDialog.Builder netSettingsDialog = new AlertDialog.Builder(context);
                netSettingsDialog.setTitle(R.string.app_name);
                netSettingsDialog.setMessage(R.string.netSettingsDlgMessage);
                netSettingsDialog.setCancelable(false);
                netSettingsDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        //removeDialog(PROGRESS_DIALOG);
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

            case (PROGRESS_DIALOG):
                ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage(getText(R.string.loading));
                return progressDialog;

            case (ILLEGAL_DATA_DIALOD):
                AlertDialog.Builder msgDlg = new AlertDialog.Builder(context);
                msgDlg.setTitle(R.string.futureTitle);
                msgDlg.setMessage(R.string.futureMsg);
                msgDlg.setCancelable(false);
                msgDlg.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onDate.setTimeInMillis(System.currentTimeMillis());

                        dialogInterface.dismiss();
                    }
                });
                return msgDlg.create();
            case (NOT_RESPOND_DIALOG):
                AlertDialog.Builder notRespondDlg = new AlertDialog.Builder(context);
                notRespondDlg.setTitle(R.string.notRespondDlgTitle);
                notRespondDlg.setMessage(R.string.notRespondDlgMsg);
                notRespondDlg.setCancelable(true);
                notRespondDlg.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        getInfo(onDate);
                    }
                });
                return notRespondDlg.create();
        }
        return null; //super.onCreateDialog(id);
    }


    private void goToNetsettings() {
        Intent netSettings = new Intent("android.settings.WIRELESS_SETTINGS");
        startActivity(netSettings);
//        getInfo(onDate);
    }
    private DatePickerDialog.OnDateSetListener cDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            Calendar newDate = Calendar.getInstance();
            newDate.set(Calendar.YEAR, year);
            newDate.set(Calendar.MONTH, month);
            newDate.set(Calendar.DAY_OF_MONTH, day);
            if (!newDate.equals(onDate)) {
                onDate = newDate;
//                getInfo(newDate);
            }
        }

    };


}
