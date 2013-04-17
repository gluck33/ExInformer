package ru.openitr.exinformer;

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
 * <p/>
 * User: Oleg Balditsyn
 * Date: 18.12.12
 * Time: 15:17
 */
public class CurrencyDbAdapter {
    //private static final String TAG = "exInformer";
    private static final String DATABASE_NAME = "exInformer.db";
    private static String CURRENCY_TABLE = "curtable";
    private static final int DATABASE_VERSION = 3;
    // Имена столбцов
    public static final String KEY_ID = "_id";
    public static final String KEY_CODE = "vCode";
    public static final String KEY_CHARCODE = "vchCode";
    public static final String KEY_NOMINAL = "vNom";
    public static final String KEY_VCURS = "vCurs";
    public static final String KEY_VNAME = "vName";
    public static final String KEY_DATE = "vDate";
    private static final String KEY_IMAGE_URI = "vFlagImageUri";
    public static final String KEY_VISIBLE = "vVisible";
    public static final String KEY_ORDER = "vOrder";

    // Индексы столбцов
    public static final int VALINDEX_COLUMN = 0;
    public static final int VALCODE_COLUMN = 1;
    public static final int VALCHARCODE_COLUMN = 2;
    public static final int VALNOMINAL_COLUMN = 3;
    public static final int VALCURS_COLUMN = 4;
    public static final int VALNAME_COLUMN = 5;
    public static final int VALDATE_COLUMN = 6;
    public static final int FLAGURI_COLUMN = 7;
    public static final int VISIBLE_COLUMN = 8;
    public static final int ORDER_COLUMN = 9;


    private static final String CREATE_CUR_TABLE = "create table " +
            CURRENCY_TABLE + " (" +
            KEY_ID + " integer primary key autoincrement, " +
            KEY_CODE + " ineger, " +
            KEY_CHARCODE + " text," +
            KEY_NOMINAL + " integer," +
            KEY_VCURS + " real," +
            KEY_VNAME + " text," +
            KEY_DATE + " long, " +
            KEY_IMAGE_URI + " text," +
            KEY_VISIBLE + " integer," +
            KEY_ORDER + " integer" +
            ");";
    public static final String[] ALL_COLUMNS = {KEY_ID, KEY_CODE, KEY_CHARCODE, KEY_NOMINAL, KEY_VCURS, KEY_VNAME, KEY_DATE, KEY_IMAGE_URI, KEY_VISIBLE, KEY_ORDER};
    public static final String[] ALL_VISIBLE_COLUMNS = {KEY_IMAGE_URI, KEY_CHARCODE, KEY_NOMINAL, KEY_VCURS, KEY_VNAME};
    //    private static final String DELETE_AUTO_INCREMENT = "DELETE FROM sqlite_sequence WHERE name='"+CURRENCY_TABLE+"';";
    private SQLiteDatabase db;
    private curDbHelper dbHelper;


    private static class curDbHelper extends SQLiteOpenHelper {

        public curDbHelper(Context context, String name,
                           SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(CREATE_CUR_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int __newVersion) {
            _db.execSQL("drop table if exists " + CURRENCY_TABLE);
            onCreate(_db);

        }
    }

    /**
     * Конструктор
     */
    public CurrencyDbAdapter(Context _context) {
        dbHelper = new curDbHelper(_context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Открыть базу
     */
    public CurrencyDbAdapter open() throws SQLiteException {
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = dbHelper.getReadableDatabase();
        }
        return this;
    }

    /**
     * Закрыть базу
     */
    public void close() {
        db.close();
    }

    /**
     * Запихивает экземпляр Icurrency в ContentValues для дальнейшего использования в
     * разных запросах.
     * @param _icurrency
     * @return Запихнутые в ContentValues данные о валюте.
     */

    private ContentValues currencyToContentValues(Icurrency _icurrency){
        ContentValues cv = new ContentValues();
        cv.put(KEY_CODE, _icurrency.getvCode());
        cv.put(KEY_CHARCODE, _icurrency.getVchCode());
        cv.put(KEY_NOMINAL, _icurrency.getvNom());
        cv.put(KEY_VCURS, _icurrency.getvCurs());
        cv.put(KEY_VNAME, _icurrency.getvName());
        cv.put(KEY_DATE, _icurrency.getvDate().getTime());
        return cv;
    }


    /**
     * Вставить строку в таблицу
     */

    public long insertCurrencyRow(Icurrency icurrency) {
        String vChCode = icurrency.getVchCode().toLowerCase();
        String imageURI = "android.resource://ru.openitr.exinformer/drawable/f_" + vChCode;
        Integer rowId = 0;
        Cursor cursor = db.rawQuery("select count (*) as rowid from " + CURRENCY_TABLE, null);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            rowId = cursor.getInt(0);
        }
        ContentValues newCurRow = currencyToContentValues(icurrency);
        newCurRow.put(KEY_IMAGE_URI, imageURI);
        newCurRow.put(KEY_VISIBLE, 1);
        newCurRow.put(KEY_ORDER, rowId + 1);
        return db.insert(CURRENCY_TABLE, null, newCurRow);
    }

    /**
     * Вставляет строку в таблицу curtable
     * @param _cv Contentvalues
     * @return
     */
    public long insertCurrencyRow(ContentValues _cv) {
        String imageURI = "android.resource://ru.openitr.exinformer/drawable/f_" + _cv.getAsString("vChCode");
        Integer rowId = 0;
        Cursor cursor = db.rawQuery("select count (*) as rowid from " + CURRENCY_TABLE, null);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            rowId = cursor.getInt(0);
        }
        _cv.put(KEY_IMAGE_URI, imageURI);
        _cv.put(KEY_VISIBLE, 1);
        _cv.put(KEY_ORDER, rowId + 1);
        return db.insert(CURRENCY_TABLE, null, _cv);
    }



    /**
     * Изменяет строку данных о валюте в таблице.
     * @param _icurrency
     * @return количество измененных строк.
     */


    public int updateCurrencyRow (Icurrency _icurrency){
        ContentValues cv = currencyToContentValues(_icurrency);
        return db.update(CURRENCY_TABLE, cv, KEY_CHARCODE+" = ?", new String[]{_icurrency.getVchCode()});
    }

    public int updateCurrencyRow(ContentValues _cv, String selection, String[] selectionArgs){
       return db.update(CURRENCY_TABLE, _cv, selection, selectionArgs);
    }


    /**
     * Удалить стоку
     *
     * @param _rowIndex - код валюты
     * @return удалено или нет.
     */

    public boolean removeCurRow(int _rowIndex) {
        return db.delete(CURRENCY_TABLE, KEY_CODE + "=" + _rowIndex, null) > 0;
    }

    /**
     * Получить все строки таблицы
     *
     * @return Курсор.
     */

    public Cursor getAllCurRowsCursor() {

        return db.query(CURRENCY_TABLE, ALL_COLUMNS, KEY_VISIBLE+" = ?", new String[]{"1"}, null, null, KEY_ORDER);
    }

    /**
     * Поиск в таблице по индесу(порядку).
     *
     * @param rowIndex - Номер строки в таблице. 1-35
     * @return
     */

    public Icurrency getCurrency(int rowIndex) {
        this.open();
        Cursor cursor = db.query(true, CURRENCY_TABLE, ALL_COLUMNS, KEY_ID + "=" + rowIndex, null, null, null, null, null);
        if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
            throw new SQLiteException("No to do row found: " + rowIndex);
        }
        String vName = cursor.getString(VALNAME_COLUMN);   // Наименование валюты.
        int vNom = cursor.getInt(VALNOMINAL_COLUMN);     // Номинал.
        Float vCurs = cursor.getFloat(VALCURS_COLUMN);    // Курс.
        String vchCode = cursor.getString(VALCHARCODE_COLUMN); // Код валюты.
        int vCode = cursor.getInt(VALCODE_COLUMN); // Внутренний код валюты.
        java.util.Date vDate = new java.util.Date(new Date(cursor.getLong(VALDATE_COLUMN)).getTime()); // Дата курса.
        cursor.close();
        return new Icurrency(vName, vNom, vCurs, vchCode, vCode, vDate);
    }

    /**
     * Поиск в таблице по буквенному коду валютыю
     *
     * @param valChCode - Буквенный код валюты.
     * @return Экземпляр Icurrency.
     */

    public Icurrency getCurency(String valChCode) {
        Cursor cursor = db.query(true, CURRENCY_TABLE, ALL_COLUMNS, KEY_CHARCODE + "=" + valChCode, null, null, null, null, null);
        if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
            throw new SQLiteException("No to do row found for code: " + valChCode);
        }
        int id = cursor.getInt(VALINDEX_COLUMN);
        return getCurrency(id);
    }

    public Cursor query(String[] projection, String selection, String[] selectionArgs, String sortOrder){
        this.open();
        Cursor res = db.query(CURRENCY_TABLE,projection, selection , selectionArgs, null, null, sortOrder);
        return res;

    }


    /**
     * @return Дата курса в базе.
     */

    public Date getCursDate() {
        Date curD;
        try {
            Icurrency cur = getCurrency(1);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return new Date(0);
        }
        curD = new Date(getCurrency(1).getvDate().getTime());
        return curD;//new Date(getCurrency(1).getvDate().getTime());
    }

    public boolean deleteAllRows() {
        return (db.delete(CURRENCY_TABLE, null, null) > 0) & (db.delete("sqlite_sequence", "name='" + CURRENCY_TABLE + "'", null) > 0);
    }

    public boolean needUpdate(java.util.Date onDate) {
        Calendar cursDate = Calendar.getInstance();
        cursDate.setTime(getCursDate());
        Calendar date = Calendar.getInstance();
        date.setTime(onDate);
        cursDate.set(Calendar.MILLISECOND, 0);
        cursDate.set(Calendar.SECOND, 0);
        cursDate.set(Calendar.MINUTE, 0);
        cursDate.set(Calendar.HOUR, 0);
        date.set(Calendar.MILLISECOND, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.HOUR, 0);
        return cursDate.compareTo(date) != 0;
    }

}
