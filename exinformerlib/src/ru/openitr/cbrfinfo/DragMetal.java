package ru.openitr.cbrfinfo;
import android.content.ContentValues;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by oleg on 30.10.13.
 */
public class DragMetal {
    private final static String [] MetallEngNames = {"Gold", "Silver", "Platinum", "Palladium"};
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
        return MetallEngNames [this.code];
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
}
