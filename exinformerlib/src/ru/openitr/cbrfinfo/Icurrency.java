package ru.openitr.cbrfinfo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: oleg
 * Date: 10.12.12
 * Time: 8:41
 * To change this template use File | Settings | File Templates.
 */
public class Icurrency {
    static final String LOG_TAG = "CBInfo";
    private String vName;   // Наименование валюты.
    private Float vCurs;    // Курс.
    private String vchCode; // Код валюты.
    private int vCode; // Внутренний код валюты.
    private Calendar vDate; // Дата курса.
    private String vFlag; // Ресурс где хранится изображение флага.

    public Icurrency() {
        this.vName = "";
        this.vCurs = 0f;
        this.vchCode = "";
        this.vCode = 0;
    }



    public Icurrency(String vName, Float vCurs, String vchCode, int vCode, Calendar vDate) {
        this.vName = vName;
        this.vCurs = vCurs;
        this.vchCode = vchCode;
        this.vCode = vCode;
        this.vDate = vDate;
    }

    public String getvName() {
        return this.vName;
    }


    public Float getvCurs() {
        return this.vCurs;
    }

    public String getVchCode() {
        return this.vchCode;
    }

    public void setvName(String vName) {
        this.vName = vName;
    }


    public void setvCurs(Float vCurs) {
        this.vCurs = vCurs;
    }

    public void setVchCode(String vchCode) {
        this.vchCode = vchCode;
    }

    public int getvCode() {
        return vCode;
    }

    public void setvCode(int vCode) {
        this.vCode = vCode;
    }

    @Override
    // TODO Продумать и переделать toString
    public String toString() {
        return String.format("%s %s %s", this.vchCode, this.vName, this.vCurs);
    }

    public String vCursAsString() {
        return Float.toString(this.vCurs);
    }

    public Calendar getvDate() {
        return vDate;
    }

    public void setvDate(Calendar vDate) {
        this.vDate = vDate;
    }

    public String vDateAsString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dat = sdf.format(this.vDate);
        return dat;
    }

    /**
     * Возвращает экземпляр класса Icurrency с заполненными значениями полей, взятых из ContentValues
     * @param _cv - ContentValues.
     *            Должен содержать ключи  vName - Наименование валюты
     *                                    vNom - Номинал
     *                                    vCurs - Курс
     *                                    vchCode - Буквенный код валюты
     *                                    vCode - Внутренний код валюты
     *                                    vDate - Дата курса.
     * @return
     * @throws IllegalArgumentException
     */
    public Icurrency parseFromContenValues (ContentValues _cv) throws IllegalArgumentException {
        if (_cv.containsKey("vName")) {
            this.vName = _cv.getAsString("vName");
        }
        else throw new IllegalArgumentException("The argument must contain a key vNom...");
        if (_cv.containsKey("vCurs")) {
            this.vCurs = _cv.getAsFloat("vCurs");
        }
        else throw new IllegalArgumentException("The argument must contain a key vCurs...");
        if (_cv.containsKey("vchCode")) {
            this.vchCode = _cv.getAsString("vchCode");
        }
        else throw new IllegalArgumentException("The argument must contain a key vchCode...");
        if (_cv.containsKey("vCode")) {
            this.vCode = _cv.getAsInteger("vCode");
        }
        else throw new IllegalArgumentException("The argument must contain a key vCode...");
        if (_cv.containsKey("vDate")) {
            //this.vDate = new SimpleDateFormat("yyyy-MM-dd").parse(_cv.getAsString("vDate"));
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.parse(_cv.getAsString("vDate"));
                this.vDate = sdf.getCalendar();
            } catch (ParseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        else throw new IllegalArgumentException("The argument must contain a key vDate...");
        return this;
    }

    /**
     * Запихивает экземпляр Icurrency в ContentValues для дальнейшего использования в
     * разных запросах.
     * @param
     * @return Запихнутые в ContentValues данные о валюте.
     */

    public ContentValues asContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(CbInfoDb.CUR_KEY_CODE, this.getvCode());
        cv.put(CbInfoDb.CUR_KEY_CHARCODE, this.getVchCode());
        cv.put(CbInfoDb.CUR_KEY_VCURS, this.getvCurs());
        cv.put(CbInfoDb.CUR_KEY_VNAME, this.getvName());
        cv.put(CbInfoDb.CUR_KEY_DATE, this.getvDate().getTimeInMillis());
        return cv;
    }

    public static boolean isNeedUpdate(Calendar onDate, Context context){
        Cursor c = context.getContentResolver().query(CBInfoProvider.CURRENCY_CONTENT_URI,CbInfoDb.CUR_ALL_COLUMNS,null,null,null);
        if (!c.moveToFirst()){
            c.close();
            return true;
        }
        Calendar infoDate = Calendar.getInstance();
        infoDate.setTimeInMillis(c.getLong(CbInfoDb.VALDATE_COLUMN));
        if (onDate.get(Calendar.DAY_OF_YEAR)!= infoDate.get(Calendar.DAY_OF_YEAR)){
            c.close();
            return true;
        }
        c.close();
        return false;
    }

    public  static Calendar getDateInBase(Context context) {
        Calendar exDate = Calendar.getInstance();
        exDate.setTimeInMillis(0);
        Cursor cursor = (context.getContentResolver().query(CBInfoProvider.CURRENCY_CONTENT_URI, new String[]{CbInfoDb.CUR_KEY_DATE}, null, null, null));
        try {
            cursor.moveToFirst();
            exDate.setTimeInMillis(cursor.getLong(0));
        }
        finally {
            cursor.close();
            return exDate;
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




}
