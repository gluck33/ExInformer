package ru.openitr.cbrfinfo;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Created by
 * User: oleg
 * Date: 29.08.13
 * Time: 11:38
 */

public class MetallArrayAdapter extends ArrayAdapter<DragMetal> {
    private final String pkgName = getContext().getPackageName();
    private static String [] metalNames = new String[4];
    private Currency measurement = Currency.getInstance("RUB");

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
            holder.metalDeltaImage = (ImageView) v.findViewById(R.id.MetalDeltaimageView);
            holder.metalDeltaText = (TextView) v.findViewById(R.id.MetalDeltaTextView);
            v.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) v.getTag();
        DragMetal metalItem = getItem(position);
        String name = metalNames[metalItem.getCode() - 1];
        String measureString = measurement.getSymbol(Locale.getDefault());
        String imageUriString = "android.resource://" + pkgName + "/drawable/" + metalItem.getMetallEngName();
        holder.metalImage.setImageURI(Uri.parse(imageUriString));
        holder.metalName.setText(name);
        holder.metalPrice.setText(String.valueOf(metalItem.getPrice()) + " " +measureString);
        Float mDelta = metalItem.getmDelta();
        holder.metalDeltaText.setText(String.valueOf(mDelta));
        if (mDelta > 0) {holder.metalDeltaImage.setImageURI(Uri.parse("android.resource://" + pkgName + "/drawable/up_triangle"));}
        else if (mDelta < 0){holder.metalDeltaImage.setImageURI(Uri.parse("android.resource://" + pkgName + "/drawable/down_triangle"));}
        return v;
    }
    private class ViewHolder {
        public TextView metalName;
        public TextView metalPrice;
        public ImageView metalImage;
        public ImageView metalDeltaImage;
        public TextView metalDeltaText;
    }

}
