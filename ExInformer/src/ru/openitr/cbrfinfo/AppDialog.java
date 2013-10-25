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
import android.support.v4.app.FragmentActivity;

import java.util.Calendar;

/**
 * Created by oleg on 22.10.13.
 */
public class AppDialog extends DialogFragment {
    static final public int DATE_DIALOG = 1;
    static final public int NETSETTINGS_DIALOG = 2;
    static final public int PROGRESS_DIALOG = 3;
    static final public int ILLEGAL_DATA_DIALOD = 4;
    static final public int NOT_RESPOND_DIALOG = 5;
    static Calendar onDate;
    protected int dialogId;
    int year;
    int month;
    int day;
    DatePickerDialog.OnDateSetListener ondateSet;

    public void setNotRespondPositiveOnClick(DialogInterface.OnClickListener notRespondPositiveOnClick) {
        this.notRespondPositiveOnClick = notRespondPositiveOnClick;
    }

    DialogInterface.OnClickListener notRespondPositiveOnClick;
    protected AppDialog(int dialogId) {
        this.dialogId = dialogId;
    }

    public static AppDialog newInstance(int id) {
        AppDialog dialog = new AppDialog(id);
        return dialog;
    }

    public void setCallBack(DatePickerDialog.OnDateSetListener ondate) {
        ondateSet = ondate;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        year = args.getInt("year");
        month = args.getInt("month");
        day = args.getInt("day");

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        switch (dialogId) {
            case (PROGRESS_DIALOG):
                final ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage(getString(R.string.loading));
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                return dialog;
            case DATE_DIALOG:
                return new DatePickerDialog(getActivity(), ondateSet, year, month, day);
            case NETSETTINGS_DIALOG:
                return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.netSettingsDlgMessage)
                        .setCancelable(false)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                dialogInterface.cancel();
                            }
                        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                goToNetsettings();
                            }
                        })

                        .create();
            case ILLEGAL_DATA_DIALOD:
                return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.futureTitle)
                        .setMessage(R.string.futureMsg)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                onDate.setTimeInMillis(System.currentTimeMillis());

                                dialogInterface.dismiss();
                            }
                        })
                        .create();
            case NOT_RESPOND_DIALOG:
                return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.notRespondDlgTitle)
                        .setMessage(R.string.notRespondDlgMsg)
                        .setCancelable(true)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                            }
                        }).setPositiveButton(R.string.ok, notRespondPositiveOnClick)
                        .create();
        }
        return null;
    }

        private void goToNetsettings() {
        Intent netSettings = new Intent("android.settings.WIRELESS_SETTINGS");
        startActivity(netSettings);
//        getInfo(onDate);
    }

}



/*

    public Dialog onCreatedialog(Bundle savedInstanceState){
        switch (id) {

            case (DATA_DIALOG):
                DatePickerDialog dpd;
                dpd = new DatePickerDialog(getActivity(), cDateSetListener, onDate.get(Calendar.YEAR),
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

*/
//    private void goToNetsettings() {
//        Intent netSettings = new Intent("android.settings.WIRELESS_SETTINGS");
//        startActivity(netSettings);
////        getInfo(onDate);
//    }
//    private DatePickerDialog.OnDateSetListener cDateSetListener = new DatePickerDialog.OnDateSetListener() {
//        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
//            Calendar newDate = Calendar.getInstance();
//            newDate.set(Calendar.YEAR, year);
//            newDate.set(Calendar.MONTH, month);
//            newDate.set(Calendar.DAY_OF_MONTH, day);
//            if (!newDate.equals(onDate)) {
//                onDate = newDate;
////                getInfo(newDate);
//            }
//        }
//
//    };



