package ru.openitr.exinformer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

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
    public static final String DATABASE_NAME = "exInformer.db";
    public static String CURRENCY_TABLE = "curtable";
    public static final int DATABASE_VERSION = 4;
    // Имена столбцов
    public static final String KEY_ID = "_id";
    public static final String KEY_CODE = "vCode";
    public static final String KEY_CHARCODE = "vchCode";
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
    public static final int VALCURS_COLUMN = 3;
    public static final int VALNAME_COLUMN = 4;
    public static final int VALDATE_COLUMN = 5;
    public static final int FLAGURI_COLUMN = 6;
    public static final int VISIBLE_COLUMN = 7;
    public static final int ORDER_COLUMN = 8;


    protected static final String CREATE_CUR_TABLE = "create table " +
            CURRENCY_TABLE + " (" +
            KEY_ID + " integer primary key autoincrement, " +
            KEY_CODE + " ineger, " +
            KEY_CHARCODE + " text," +
            KEY_VCURS + " real," +
            KEY_VNAME + " text," +
            KEY_DATE + " long, " +
            KEY_IMAGE_URI + " text," +
            KEY_VISIBLE + " integer," +
            KEY_ORDER + " integer" +
            ");";
    public static final String[] ALL_COLUMNS = {KEY_ID, KEY_CODE, KEY_CHARCODE, KEY_VCURS , KEY_VNAME, KEY_DATE, KEY_IMAGE_URI, KEY_VISIBLE, KEY_ORDER};
    public static final String[] ALL_VISIBLE_COLUMNS = {KEY_IMAGE_URI, KEY_CHARCODE, KEY_VCURS, KEY_VNAME};
    //    private static final String DELETE_AUTO_INCREMENT = "DELETE FROM sqlite_sequence WHERE name='"+CURRENCY_TABLE+"';";
    private SQLiteDatabase db;
    private curDbHelper dbHelper;
    private boolean cursorOpened;


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
        cursorOpened = false;
    }

    /**
     * Открыть базу
     */
    public CurrencyDbAdapter open() throws SQLiteException {
        try {
            db = dbHelper.getWritableDatabase();
            cursorOpened = true;
        } catch (SQLiteException e) {
            db = dbHelper.getReadableDatabase();
        }
        return this;
    }

    /**
     * Закрыть базу
     */
    public void close() {
        if (db!=null)
            db.close();
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
        ContentValues newCurRow = icurrency.toContentValues();
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
        String imageURI = "android.resource://ru.openitr.exinformer/drawable/f_" + _cv.getAsString(KEY_CHARCODE).toLowerCase();
        Integer rowId = 0;
        _cv.put(KEY_IMAGE_URI, imageURI);
        _cv.put(KEY_VISIBLE, 1);
        if (!_cv.containsKey(KEY_ORDER)){
            Cursor cursor = db.rawQuery("select count (*) as rowid from " + CURRENCY_TABLE, null);
            if (cursor.getCount() != 0 && cursor.moveToFirst()) {
                rowId = cursor.getInt(0);
            }

            _cv.put(KEY_ORDER, rowId + 1);
        }
        else{

        }
        return db.insert(CURRENCY_TABLE, null, _cv);
    }



    /**
     * Изменяет строку данных о валюте в таблице.
     * @param _icurrency
     * @return количество измененных строк.
     */


    public int updateCurrencyRow (Icurrency _icurrency){
        ContentValues _cv = _icurrency.toContentValues();
        String imageURI = "android.resource://ru.openitr.exinformer/drawable/f_" + _cv.getAsString(KEY_CHARCODE).toLowerCase();
        _cv.put(KEY_IMAGE_URI, imageURI);
        return db.update(CURRENCY_TABLE, _cv, KEY_CHARCODE+" = ?", new String[]{_icurrency.getVchCode()});
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
        return db.delete(CURRENCY_TABLE, KEY_CODE + " = " + _rowIndex, null) > 0;
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
        Float vCurs = cursor.getFloat(VALCURS_COLUMN);    // Курс.
        String vchCode = cursor.getString(VALCHARCODE_COLUMN); // Код валюты.
        int vCode = cursor.getInt(VALCODE_COLUMN); // Внутренний код валюты.
        Calendar vDate = Calendar.getInstance();
        vDate.setTimeInMillis(cursor.getLong(VALDATE_COLUMN)); // Дата курса.
        cursor.close();
        this.close();
        return new Icurrency(vName, vCurs, vchCode, vCode, vDate);
    }

    /**
     * Поиск в таблице по буквенному коду валюты.
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

//    public Cursor query(String[] projection, String selection, String[] selectionArgs, String sortOrder){
//        if (!cursorOpened)
//            this.open();
//        Cursor res = db.query(CURRENCY_TABLE,projection, selection , selectionArgs, null, null, sortOrder);
//        return res;
//
//    }


    /**
     * @return Дата курса в базе.
     */

    public Calendar getCursDate() {
        Calendar curD = Calendar.getInstance();
        try {
            curD = getCurrency(1).getvDate();
            return curD;
        } catch (SQLiteException e) {
            e.printStackTrace();
            curD.setTimeInMillis(0);
            return curD;
        }
    }

    public boolean deleteAllRows() {
        return (db.delete(CURRENCY_TABLE, null, null) > 0) & (db.delete("sqlite_sequence", "name='" + CURRENCY_TABLE + "'", null) > 0);
    }
     /**
      * Метод сравнивает дату данных в базе с параметром.
      * @param onDate - Дата с которой нужно сравнить.
      *
      * @return Если Год и день года равны возвращает false. В остальных случаях true.
     * */
    public boolean isNeedUpdate(Calendar onDate) {
        Calendar cursDate = getCursDate();
        return !(ExtraCalendar.isEqualDays(cursDate, onDate));

    }

}
