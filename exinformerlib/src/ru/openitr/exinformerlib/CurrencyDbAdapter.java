package ru.openitr.exinformerlib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.Date;
import java.util.Calendar;

/**
 * Db adapter for currency.
 *
 * User: Oleg Balditsyn
 * Date: 18.12.12
 * Time: 15:17
 *
 */
public class CurrencyDbAdapter {
    //private static final String TAG = "exInformer";
    private static final String DATABASE_NAME = "exInformer.db";
    private static String CURRENCY_TABLE = "curtable";
    private static final int DATABASE_VERSION = 1;
    // Имена столбцов
    public static final String KEY_ID = "_id";
    public static final String KEY_CODE = "val_code";
    public static final String KEY_CHARCODE = "v_char_code";
    public static final String KEY_NOMINAL = "v_nom";
    public static final String KEY_VCURS = "v_curs";
    public static final String KEY_VNAME = "v_name";
    public static final String KEY_DATE = "v_curs_date";
    // Индексы столбцов
    public static final int VALINDEX_COLUMN = 0;
    public static final int VALCODE_COLUMN = 1;
    public static final int VALCHARCODE_COLUMN = 2;
    public static final int VALNOMINAL_COLUMN = 3;
    public static final int VALCURS_COLUMN = 4;
    public static final int VALNAME_COLUMN = 5;
    public static final int VALDATE_COLUMN = 6;
    public static final int FLAGURI_COLUMN = 7;
    private static final String KEY_IMAGE_URI = "v_flag_image";
    private static final String CREATE_CUR_TABLE = "create table " +
            CURRENCY_TABLE + " ("+
            KEY_ID + " integer primary key autoincrement, " +
            KEY_CODE + " ineger, " +
            KEY_CHARCODE + " text,"+
            KEY_NOMINAL + " integer," +
            KEY_VCURS + " real,"+
            KEY_VNAME + " text,"+
            KEY_DATE + " long, "+
            KEY_IMAGE_URI + " text);";
    public static final String[] ALL_COLUMNS = {KEY_ID, KEY_CODE, KEY_CHARCODE, KEY_NOMINAL, KEY_VCURS, KEY_VNAME, KEY_DATE, KEY_IMAGE_URI};
    public static final String[] ALL_VISIBLE_COLUMNS = {KEY_IMAGE_URI, KEY_CHARCODE, KEY_NOMINAL, KEY_VCURS, KEY_VNAME};
//    private static final String DELETE_AUTO_INCREMENT = "DELETE FROM sqlite_sequence WHERE name='"+CURRENCY_TABLE+"';";
    private SQLiteDatabase db;
    private curDbHelper dbHelper;


    private static class curDbHelper extends SQLiteOpenHelper {

        public curDbHelper (Context context, String name,
                            SQLiteDatabase.CursorFactory factory, int version){
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(CREATE_CUR_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int __newVersion) {
            _db.execSQL("drop table if exist "+ CURRENCY_TABLE);
            onCreate(_db);

        }
    }
    /**
     * Конструктор
     * */
    public CurrencyDbAdapter(Context _context){
        dbHelper = new curDbHelper(_context, DATABASE_NAME, null, DATABASE_VERSION);
        this.open();
    }
    /**
     * Открыть базу
     */
    public CurrencyDbAdapter open() throws SQLiteException {
        try {
            db = dbHelper.getWritableDatabase();
          } catch (SQLiteException e){
            db = dbHelper.getReadableDatabase();
        }
        return this;
    }

    /**
     * Закрыть базу
     */
    public void close(){
        db.close();
    }
    /**
     * Вставить строку в таблицу
     */
    public long insertCurRow(Currency currency){
        String vChCode = currency.getVchCode().toLowerCase();
        String imageURI = "android.resource://ru.openitr.exinformer/drawable/f_"+vChCode;
        ContentValues newCurRow = new ContentValues();
        newCurRow.put(KEY_CODE,currency.getvCode());
        newCurRow.put(KEY_CHARCODE,currency.getVchCode());
        newCurRow.put(KEY_NOMINAL, currency.getvNom());
        newCurRow.put(KEY_VCURS,currency.getvCurs());
        newCurRow.put(KEY_VNAME,currency.getvName());
        newCurRow.put(KEY_DATE, currency.getvDate().getTime());
        newCurRow.put(KEY_IMAGE_URI, imageURI);
        return db.insert(CURRENCY_TABLE,null,newCurRow);
    }

    /**
     * Удалить стоку
     * @param _rowIndex - код валюты
     * @return удалено или нет.
     */

    public boolean removeCurRow(int _rowIndex) {
        return db.delete(CURRENCY_TABLE, KEY_CODE + "=" + _rowIndex, null) > 0;
    }

    /**
     * Получить все строки таблицы
     * @return Курсор.
     */
    public Cursor getAllCurRowsCursor(){

        return  db.query(CURRENCY_TABLE, ALL_COLUMNS, null, null, null, null, null);
    }

    /**
     * Поиск в таблице по индесу(порядку).
     * @param rowIndex - Номер строки в таблице. 1-35
     * @return
     */

    public Currency getCurrency(int rowIndex){
       Cursor cursor = db.query(true, CURRENCY_TABLE, ALL_COLUMNS, KEY_ID + "=" + rowIndex, null,null,null,null,null);
       if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
           throw new SQLiteException("No to do row found: "+rowIndex);
        }
        String  vName   = cursor.getString(VALNAME_COLUMN);   // Наименование валюты.
        int     vNom    = cursor.getInt(VALNOMINAL_COLUMN);     // Номинал.
        Float   vCurs   = cursor.getFloat(VALCURS_COLUMN);    // Курс.
        String  vchCode = cursor.getString(VALCHARCODE_COLUMN); // Код валюты.
        int     vCode   = cursor.getInt(VALCODE_COLUMN); // Внутренний код валюты.
        java.util.Date  vDate = new java.util.Date(new Date(cursor.getLong(VALDATE_COLUMN)).getTime()); // Дата курса.
        cursor.close();
        return new Currency(vName,vNom,vCurs,vchCode,vCode,vDate);
    }

    /**
     * Поиск в таблице по буквенному коду валютыю
     * @param valChCode - Буквенный код валюты.
     * @return Экземпляр Currency.
     */

    public Currency getCurency(String valChCode){
        Cursor cursor = db.query(true, CURRENCY_TABLE, ALL_COLUMNS, KEY_CHARCODE + "=" + valChCode, null,null,null,null,null);
        if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
            throw new SQLiteException("No to do row found for code: "+valChCode);
        }
        int id = cursor.getInt(VALINDEX_COLUMN);
        return getCurrency(id);
    }
    /**
     * @return  Дата курса в базе.
     */

    public Date getCursDate() {
        Date curD;
        try {
          Currency cur = getCurrency(1);
      } catch (SQLiteException e) {
          e.printStackTrace();
          return new Date(0);
      }
        curD = new Date (getCurrency(1).getvDate().getTime());
        return curD;//new Date(getCurrency(1).getvDate().getTime());
    }

    public boolean deleteAllRecord() {
        return (db.delete(CURRENCY_TABLE, null, null) > 0) & (db.delete("sqlite_sequence","name='"+ CURRENCY_TABLE +"'",null)>0);
    }

    public boolean needUpdate (java.util.Date onDate){
        Calendar cursDate = Calendar.getInstance();
        cursDate.setTime(getCursDate());
        Calendar date = Calendar.getInstance();
        date.setTime(onDate);
        cursDate.set(Calendar.MILLISECOND,0);
        cursDate.set(Calendar.SECOND,0);
        cursDate.set(Calendar.MINUTE,0);
        cursDate.set(Calendar.HOUR,0);
        date.set(Calendar.MILLISECOND,0);
        date.set(Calendar.SECOND,0);
        date.set(Calendar.MINUTE,0);
        date.set(Calendar.HOUR,0);
        return cursDate.compareTo(date) !=0 ;
    }

}
