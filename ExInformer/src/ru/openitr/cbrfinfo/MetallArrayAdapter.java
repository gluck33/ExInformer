package ru.openitr.cbrfinfo;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by
 * User: oleg
 * Date: 29.08.13
 * Time: 11:38
 */

public class MetallArrayAdapter extends ArrayAdapter<DragMetal> {
    private final String pkgName = getContext().getPackageName();
    private static String [] metalNames = new String[4];

    public MetallArrayAdapter(Context context, List<DragMetal> metals) {
        super(context, R.layout.metall_prices_layout, R.id.MetalNameView, metals);
        metalNames = context.getResources().getStringArray(R.array.metall_names);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        if (v != convertView && v != null){
            ViewHolder holder = new ViewHolder();
            holder.metalImage = (ImageView) v.findViewById(R.id.metalIcon);
            holder.metalName = (TextView) v.findViewById(R.id.MetalNameView);
            holder.metalPrice = (TextView) v.findViewById(R.id.MetalPriceView);
            v.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) v.getTag();
        DragMetal metalItem = getItem(position);
        String name = metalNames[metalItem.getCode() - 1];
        holder.metalName.setText(name);
        holder.metalPrice.setText(String.valueOf(metalItem.getPrice()));
        return v;
    }
    private class ViewHolder {
        public TextView metalName;
        public TextView metalPrice;
        public ImageView metalImage;
    }

}
