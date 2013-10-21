package ru.openitr.cbrfinfo;

import android.content.*;
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
public class CurInfoProvider extends ContentProvider {
    SQLiteDatabase db;
    //URI
        static final String AUTHORITY = "ru.openitr.cbrfinfo.currency";
    //PATH
    static final String CURRENCY_PATH = "currencys";
    //Общий URI
    public static final Uri CURRENCY_CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+ CURRENCY_PATH);
    // Типы данных
    // набор строк
    static final String CURRENCY_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CURRENCY_PATH;
    // Одна строка
    static final String CURRENCY_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + CURRENCY_PATH;
    // UriMatcher
    // общий Uri
    static final int URI_CURRENCY = 1;
    // Uri с указанным ID
    static final int URI_CURRENCY_ID = 2;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY,CURRENCY_PATH,URI_CURRENCY);
        uriMatcher.addURI(AUTHORITY,CURRENCY_PATH+"/*",URI_CURRENCY_ID);
    }
    DBHelper dbHelper;
    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
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
            case URI_CURRENCY :
//                if (TextUtils.isEmpty(sortOrder)) {
//                    sortOrder = CurrencyDbAdapter.KEY_ORDER + " ASC";
//                }
                break;

            case URI_CURRENCY_ID:
                String vcode = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = selection + " AND " + CurrencyDbAdapter.KEY_CHARCODE +" = " + vcode;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: "+uri);

        }
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CurrencyDbAdapter.CURRENCY_TABLE,projection, selection, selectionArgs,null,null,sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(),CURRENCY_CONTENT_URI);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case URI_CURRENCY:
                return CURRENCY_CONTENT_TYPE;
            case URI_CURRENCY_ID:
                return CURRENCY_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues _cv){
        if (uriMatcher.match(uri) != URI_CURRENCY) {
            throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        long rowId = db.insert(CurrencyDbAdapter.CURRENCY_TABLE, null,_cv);
        Uri resultUri = ContentUris.withAppendedId(CURRENCY_CONTENT_URI, rowId);
//        getContext().getContentResolver().notifyChange(resultUri,null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)){
            case URI_CURRENCY:
                break;
            case URI_CURRENCY_ID:
                String _vchCode = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                  selection = CurrencyDbAdapter.KEY_CHARCODE + " = " + "'" +_vchCode + "'";
                } else {
                  selection = selection + " AND " + CurrencyDbAdapter.KEY_CHARCODE + " = " + _vchCode;
                }
            break;
            default:
                throw new IllegalArgumentException("Wrong URI: "+ uri);
        }
        db = dbHelper.getWritableDatabase();
        int result = db.update(CurrencyDbAdapter.CURRENCY_TABLE, contentValues, selection, selectionArgs);
        //getContext().getContentResolver().notifyChange(uri, null);
        //db.close();
        return result;
    }
private class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, CurrencyDbAdapter.DATABASE_NAME, null, CurrencyDbAdapter.DATABASE_VERSION);
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CurrencyDbAdapter.CREATE_CUR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        db.execSQL("drop table if exists "+CurrencyDbAdapter.CURRENCY_TABLE);
        onCreate(db);
    }
}


}
