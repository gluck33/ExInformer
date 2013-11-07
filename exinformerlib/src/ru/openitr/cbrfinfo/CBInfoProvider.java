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
 * User: Oleg balditsyn
 * Date: 01.04.13
 * Time: 11:06
 */
public class CBInfoProvider extends ContentProvider {

    SQLiteDatabase db;
//************************  Currency  *****************************************

    //URI
    static final String CUR_AUTHORITY = "ru.openitr.cbrfinfo.currency";
    //PATH
    static final String CURRENCY_PATH = "currencys";
    //Общий URI
    public static final Uri CURRENCY_CONTENT_URI = Uri.parse("content://"+ CUR_AUTHORITY +"/"+ CURRENCY_PATH);
    // Типы данных
    // набор строк
    static final String CURRENCY_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + CUR_AUTHORITY + "." + CURRENCY_PATH;
    // Одна строка
    static final String CURRENCY_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + CUR_AUTHORITY + "." + CURRENCY_PATH;
    // UriMatcher
    // общий Uri
    static final int URI_CURRENCY = 1;
    // Uri с указанным ID
    static final int URI_CURRENCY_ID = 2;



    //************************  Metals *********************************************
    //URI
        static final String MET_AUTHORITY = "ru.openitr.cbrfinfo.metals";
    //PATH
    static final String METAL_PATH = "metal";
    //Общий URI
    public static final Uri METAL_CONTENT_URI = Uri.parse("content://"+ MET_AUTHORITY +"/"+ METAL_PATH);
    // Типы данных
    // набор строк
    static final String METAL_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + MET_AUTHORITY + "." + METAL_PATH;
    // Одна строка
    static final String METAL_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + MET_AUTHORITY + "." + METAL_PATH;
    // UriMatcher
    // общий Uri
    static final int URI_METALL = 3;
    // Uri с указанным ID
    static final int URI_METAL_ID = 4;
    //************************  Metals *********************************************

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CUR_AUTHORITY,CURRENCY_PATH,URI_CURRENCY);
        uriMatcher.addURI(CUR_AUTHORITY,CURRENCY_PATH+"/*",URI_CURRENCY_ID);
        uriMatcher.addURI(MET_AUTHORITY, METAL_PATH, URI_METALL);
        uriMatcher.addURI(MET_AUTHORITY, METAL_PATH + "/*", URI_METAL_ID);
    }
    InfoDBHelper dbHelper;
    @Override
    public boolean onCreate() {
        dbHelper = new InfoDBHelper(getContext(), CbInfoDb.DATABASE_NAME, null, CbInfoDb.DATABASE_VERSION);
        return true;
    }

    @Override
    protected void finalize() throws Throwable {
        db.close();
        super.finalize();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String tableName = null;
        Uri resUri = null;
        switch (uriMatcher.match(uri)) {
            case URI_CURRENCY :
                tableName = CbInfoDb.CURRENCY_TABLE;
                resUri = CURRENCY_CONTENT_URI;
                break;

            case URI_CURRENCY_ID:
                String vcode = uri.getLastPathSegment();
                tableName = CbInfoDb.CURRENCY_TABLE;
                resUri = CURRENCY_CONTENT_URI;
                if (TextUtils.isEmpty(selection)) {
                    selection = selection + " AND " + CbInfoDb.CUR_KEY_CHARCODE +" = " + vcode;
                }
                break;
            case URI_METALL:
//                if (TextUtils.isEmpty(sortOrder)) {
//                    sortOrder = CurrencyDbAdapter.CUR_KEY_ORDER + " ASC";
//                }
                tableName = CbInfoDb.METAL_TABLE;
                resUri = CURRENCY_CONTENT_URI;
                break;

            case URI_METAL_ID:
                String mcode = uri.getLastPathSegment();
                tableName = CbInfoDb.METAL_TABLE;
                resUri = METAL_CONTENT_URI;
                if (TextUtils.isEmpty(selection)) {
                    selection = selection + " AND " + CbInfoDb.MET_KEY_CODE +" = " + mcode;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: "+uri);

        }

        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(tableName,projection, selection, selectionArgs,null,null,sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), resUri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case URI_METALL:
                return METAL_CONTENT_TYPE;
            case URI_METAL_ID:
                return METAL_CONTENT_ITEM_TYPE;
            case URI_CURRENCY:
                return CURRENCY_CONTENT_TYPE;
            case URI_CURRENCY_ID:
                return CURRENCY_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues _cv){
        String tableName = null;
        Uri resUri = null;
        if (uriMatcher.match(uri) != URI_METALL || uriMatcher.match(uri) != URI_CURRENCY) {
            throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)){
            case URI_CURRENCY:
                tableName = CbInfoDb.CURRENCY_TABLE;
                resUri = CURRENCY_CONTENT_URI;
                break;
            case URI_METALL:
                tableName = CbInfoDb.METAL_TABLE;
                resUri = METAL_CONTENT_URI;
        }
        long rowId = db.insert(tableName, null,_cv);
        Uri resultUri = ContentUris.withAppendedId(resUri, rowId);
        getContext().getContentResolver().notifyChange(resultUri,null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String tableName = null;
        String code = null;
        switch (uriMatcher.match(uri)) {
            case URI_CURRENCY:
                tableName = CbInfoDb.CURRENCY_TABLE;
                break;

            case URI_CURRENCY_ID:
                tableName = CbInfoDb.CURRENCY_TABLE;
                code = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = selection + " AND " + CbInfoDb.CUR_KEY_CODE +" = " + code;
                }
                break;
            case URI_METALL:
                tableName = CbInfoDb.METAL_TABLE;
                break;
            case URI_METAL_ID:
                tableName = CbInfoDb.METAL_TABLE;
                code = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = selection + " AND " + CbInfoDb.MET_KEY_CODE +" = " + code;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: "+uri);

        }
        db = dbHelper.getWritableDatabase();
        int id = db.delete(tableName,selection, selectionArgs);
        return id;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        String tableName = null;
        switch (uriMatcher.match(uri)){
            case URI_CURRENCY:
                tableName = CbInfoDb.CURRENCY_TABLE;
                break;

            case URI_CURRENCY_ID:
                tableName = CbInfoDb.CURRENCY_TABLE;
                String _vchCode = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    selection = CbInfoDb.CUR_KEY_CHARCODE + " = " + "'" +_vchCode + "'";
                } else {
                    selection = selection + " AND " + CbInfoDb.CUR_KEY_CHARCODE + " = " + _vchCode;
                }
                break;

            case URI_METALL:
                tableName = CbInfoDb.METAL_TABLE;
                break;

            case URI_METAL_ID:
                tableName = CbInfoDb.METAL_TABLE;
                String _mCode = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                  selection = CbInfoDb.MET_KEY_CODE + " = " + "'" +_mCode + "'";
                } else {
                  selection = selection + " AND " + CbInfoDb.MET_KEY_CODE + " = " + _mCode;
                }
            break;
            default:
                throw new IllegalArgumentException("Wrong URI: "+ uri);
        }
        db = dbHelper.getWritableDatabase();
        int result = db.update(tableName, contentValues, selection, selectionArgs);
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
        db.execSQL(CbInfoDb.CREATE_CUR_TABLE);
        db.execSQL(CbInfoDb.CREATE_METALL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        db.execSQL("drop table if exists " + CbInfoDb.CURRENCY_TABLE);
        db.execSQL("drop table if exists " + CbInfoDb.METAL_TABLE);
        onCreate(db);
    }
}


}
