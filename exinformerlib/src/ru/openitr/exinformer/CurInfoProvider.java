package ru.openitr.exinformer;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by
 * User: oleg
 * Date: 01.04.13
 * Time: 11:06
 */
public class CurInfoProvider extends ContentProvider {
    static CurrencyDbAdapter db;
    //URI
        static final String AUTHORITY = "ru.openitr.exinformer.currency";
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
    @Override
    public boolean onCreate() {
        db = new CurrencyDbAdapter(getContext());
        return true;
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
        Cursor cursor = db.query(projection, selection, selectionArgs,sortOrder);
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
        long rowId = db.insertCurrencyRow(_cv);
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
        int result = db.updateCurrencyRow(contentValues, selection, selectionArgs);
        //getContext().getContentResolver().notifyChange(uri, null);
        //db.close();
        return result;
    }
}
