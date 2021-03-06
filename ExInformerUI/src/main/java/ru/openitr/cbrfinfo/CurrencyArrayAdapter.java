package ru.openitr.cbrfinfo;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.openitr.cbrfinfo.R;

/**
 * Created by
 * User: oleg
 * Date: 29.08.13
 * Time: 11:38
 */

public class CurrencyArrayAdapter extends ArrayAdapter<Icurrency> {
    private final String pkgName = getContext().getPackageName();
    public CurrencyArrayAdapter(Context context, List<Icurrency> currencys) {
        super(context, R.layout.currencylayuot, R.id.vNameView, currencys);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        if (v != convertView && v !=null){
            ViewHolder holder = new ViewHolder();
            holder.curFlag = (ImageView) v.findViewById(R.id.drag_handle);
            holder.curName = (TextView) v.findViewById(R.id.vNameView);
            holder.curValue = (TextView) v.findViewById(R.id.vCursView);
            holder.curChCode = (TextView) v.findViewById(R.id.vChСodeView);
            holder.curDeltaImage = (ImageView) v.findViewById(R.id.cursDeltaImageView);
            holder.curDelta = (TextView) v.findViewById(R.id.cursDeltaTextView);
            v.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) v.getTag();
        Icurrency currencyItem = getItem(position);
        String vChCode = currencyItem.getVchCode().toLowerCase();
        String flagUriString = "android.resource://" + pkgName + "/drawable/f_" + vChCode;
        holder.curFlag.setImageURI(Uri.parse(flagUriString));
        holder.curName.setText(currencyItem.getvName());
        holder.curValue.setText(currencyItem.getvCurs().toString());
        holder.curChCode.setText(currencyItem.getVchCode());
        String deltaImageUriString = "android.resource://" + pkgName + "/drawable/";
        String deltaString = String.format("%f",currencyItem.getvDelta());
        if (currencyItem.getvDelta() > 0){
            deltaImageUriString = deltaImageUriString + "up_triangle";
            deltaString = "+" + deltaString;
        }else if (currencyItem.getvDelta() < 0) {
            deltaImageUriString = deltaImageUriString + "down_triangle";
        }
        holder.curDeltaImage.setImageURI(Uri.parse(deltaImageUriString));
        holder.curDelta.setText(deltaString);
        return v;
    }
    private class ViewHolder {
        public TextView curName;
        public TextView curValue;
        public TextView curChCode;
        public ImageView curFlag;
        public ImageView curDeltaImage;
        public TextView curDelta;
    }

}
