package ru.openitr.exinformerlib;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

/**
 * Created by
 * User: Oleg Balditsyn
 * Date: 20.12.12
 * Time: 14:46
 */
public class ValFromDbAdapter extends SimpleCursorAdapter implements ViewBinder {
    @SuppressWarnings("deprecation")
    public ValFromDbAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
    }

    @Override
     public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        int vChCodeIndex = cursor.getColumnIndex(CurrencyDbAdapter.KEY_CHARCODE);
        if (columnIndex == vChCodeIndex) {
            String vChCode = cursor.getString(vChCodeIndex).toLowerCase();
            String uriString = "android.resource://ru.openitr.exinformer/drawable/f_"+vChCode;
            ((ImageView)view).setImageURI(Uri.parse(uriString));
            return false;
        }
        return false;
    }
}
