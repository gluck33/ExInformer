package ru.openitr.cbrfinfo;
import android.content.ContentValues;

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
        result.put(MetInfoProvider.KEY_CODE, this.code);
        result.put(MetInfoProvider.KEY_PRICE, this.price);
        result.put(MetInfoProvider.KEY_DATE, this.onDate.getTimeInMillis());
        return result;
    }
}
