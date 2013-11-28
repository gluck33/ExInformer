package ru.openitr.cbrfinfo;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by oleg on 30.10.13.
 */
public class DragMetal {
    private final static String [] MetallEngNames = {"gold", "silver", "platinum", "palladium"};
    private int code;
    private float price;
    private Calendar onDate;

    public DragMetal(int code, float price) {
        this.code = code;
        this.price = price;
        this.onDate = Calendar.getInstance();
    }
    public DragMetal(int code, float price, Calendar dateOfPrice) {
        this.code = code;
        this.price = price;
        this.onDate = dateOfPrice;
    }


    public int getCode() {
        return code;
    }

    public String getCodeAsString() {return String.valueOf(code);}

    public String getMetallEngName() {
        return MetallEngNames [this.code-1];
    }

    public float getPrice() {
        return price;
    }

    public Calendar getOnDate() {
        return onDate;
    }

    public String getOnDateAsString(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        return sdf.format(getOnDate().getTime());
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setPrice(String price) {
        this.price = Float.parseFloat(price);
    }


    public void setOnDate(Calendar onDate) {
        this.onDate = onDate;
    }

    public ContentValues asContentValues() {
        ContentValues result = new ContentValues();
        result.put(CbInfoDb.MET_KEY_CODE, this.code);
        result.put(CbInfoDb.MET_KEY_PRICE, this.price);
        result.put(CbInfoDb.MET_KEY_DATE, this.onDate.getTimeInMillis());
        return result;
    }



    public  static Calendar getDateInBase(Context context) {
        Calendar infoDate = Calendar.getInstance();
        infoDate.setTimeInMillis(0);
        Cursor cursor = (context.getContentResolver().query(CBInfoProvider.METAL_CONTENT_URI, new String[]{CbInfoDb.MET_KEY_DATE}, null, null, null));
        try {
            cursor.moveToLast();
            infoDate.setTimeInMillis(cursor.getLong(0));
        }
        finally {
            cursor.close();
            return infoDate;
        }
    }

    public static String getDateInBaseAsString(Context context){
        String result = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        try {
            result = sdf.format(getDateInBase(context).getTime());
        } catch (Exception e){
            return result;
        }
        return result;
    }

    public static boolean isNeedUpdate(Calendar onDate, Context context){
        Cursor c = context.getContentResolver().query(CBInfoProvider.METAL_CONTENT_URI,CbInfoDb.MET_ALL_COLUMNS,null,null,null);
        if (!c.moveToFirst()){
            c.close();
            return true;

        }
        Calendar infoDate = Calendar.getInstance();
        infoDate.setTimeInMillis(c.getLong(CbInfoDb.MET_DATE_COL_NUM));
        if (onDate.get(Calendar.DAY_OF_YEAR)!= infoDate.get(Calendar.DAY_OF_YEAR)){
            c.close();
            return true;
        }
        c.close();
        return false;
    }



}
