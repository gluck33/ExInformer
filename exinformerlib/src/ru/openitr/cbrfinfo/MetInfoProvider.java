package ru.openitr.cbrfinfo;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by
 * User: oleg
 * Date: 01.04.13
 * Time: 11:06
 */
public class MetInfoProvider extends ContentProvider {
    // Имена столбцов
    public static final String KEY_ID = "_id";
    public static final String KEY_CODE = "mCode";
    public static final String KEY_PRICE = "mPrice";
    public static final String KEY_DATE = "mDate";
    private static final String KEY_IMAGE_URI = "mImageUri";
    public static final String KEY_VISIBLE = "mVisible";
    public static final String KEY_ORDER = "mOrder";

    // Индексы столбцов

    public static final int ID_COL_NUM = 0;
    public static final int CODE_COL_NUM = 1;
    public static final int PRICE_COL_NUM = 2;
    public static final int DATE_COL_NUM = 3;
    public static final int IMAGE_COL_NUM = 4;

    public static final String[] ALL_COLUMNS = {KEY_ID, KEY_CODE, KEY_PRICE, KEY_DATE, KEY_IMAGE_URI, KEY_VISIBLE, KEY_ORDER};

    // База данных

    public static final String DATABASE_NAME = "exInformer.db";
    public static String METAL_TABLE = "metaltable";
    public static final int DATABASE_VERSION = CurrencyDbAdapter.DATABASE_VERSION;

    // Скрипт создания

    protected static final String CREATE_METALL_TABLE = "create table " +
            METAL_TABLE + " (" +
            KEY_ID + " integer primary key autoincrement, " +
            KEY_CODE + " ineger, " +
            KEY_PRICE + " real," +
            KEY_DATE + " long, " +
            KEY_IMAGE_URI + " text," +
            KEY_VISIBLE + " integer," +
            KEY_ORDER + " integer" +
            ");";


    SQLiteDatabase db;
    //URI
        static final String AUTHORITY = "ru.openitr.cbrfinfo.metals";
    //PATH
    static final String METAL_PATH = "metal";
    //Общий URI
    public static final Uri METAL_CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+ METAL_PATH);
    // Типы данных
    // набор строк
    static final String METAL_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + METAL_PATH;
    // Одна строка
    static final String METAL_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + METAL_PATH;
    // UriMatcher
    // общий Uri
    static final int URI_METALL = 1;
    // Uri с указанным ID
    static final int URI_METAL_ID = 2;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, METAL_PATH, URI_METALL);
        uriMatcher.addURI(AUTHORITY, METAL_PATH +"/*", URI_METAL_ID);
    }
    InfoDBHelper dbHelper;
    @Override
    public boolean onCreate() {
        dbHelper = new InfoDBHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        return true;
    }

    @Override
    protected void finalize() throws Throwable {
        db.close();
        super.finalize();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case URI_METALL:
//                if (TextUtils.isEmpty(sortOrder)) {
//                    sortOrder = CurrencyDbAdapter.KEY_ORDER + " ASC";
//                }
                break;

            case URI_METAL_ID:
                String mcode = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = selection + " AND " + KEY_CODE +" = " + mcode;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: "+uri);

        }
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(METAL_TABLE,projection, selection, selectionArgs,null,null,sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), METAL_CONTENT_URI);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case URI_METALL:
                return METAL_CONTENT_TYPE;
            case URI_METAL_ID:
                return METAL_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues _cv){
        if (uriMatcher.match(uri) != URI_METALL) {
            throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        long rowId = db.insert(METAL_TABLE, null,_cv);
        Uri resultUri = ContentUris.withAppendedId(METAL_CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(resultUri,null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_METALL:
                break;
            case URI_METAL_ID:
                String mcode = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = selection + " AND " + KEY_CODE +" = " + mcode;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: "+uri);

        }
        db = dbHelper.getWritableDatabase();
        int id = db.delete(METAL_TABLE,selection, selectionArgs);
        return id;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)){
            case URI_METALL:
                break;
            case URI_METAL_ID:
                String _mCode = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                  selection = KEY_CODE + " = " + "'" +_mCode + "'";
                } else {
                  selection = selection + " AND " + KEY_CODE + " = " + _mCode;
                }
            break;
            default:
                throw new IllegalArgumentException("Wrong URI: "+ uri);
        }
        db = dbHelper.getWritableDatabase();
        int result = db.update(METAL_TABLE, contentValues, selection, selectionArgs);
        db.close();
        return result;
    }
private class InfoDBHelper extends SQLiteOpenHelper {

    public InfoDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_METALL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        db.execSQL("drop table if exists "+METAL_TABLE);
        onCreate(db);
    }
}


}
