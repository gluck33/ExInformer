package ru.openitr.cbrfinfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Calendar;

/**
 * Db adapter for cache information.
 *
 * User: Oleg Balditsyn
 * Date: 18.12.12
 * Time: 15:17
 */
public abstract class CbInfoDb {
    //private static final String TAG = "exInformer";
    public static final String DATABASE_NAME = "exInformer.db";
    public static String CURRENCY_TABLE = "curtable";
    public static String METAL_TABLE = "metaltable";
    public static final int DATABASE_VERSION = 6;
    // Имена столбцов таблицы валют
    public static final String CUR_KEY_ID = "_id";
    public static final String CUR_KEY_CODE = "vCode";
    public static final String CUR_KEY_CHARCODE = "vchCode";
    public static final String CUR_KEY_VCURS = "vCurs";
    public static final String CUR_KEY_VNAME = "vName";
    public static final String CUR_KEY_DATE = "vDate";
    public static final String CUR_KEY_IMAGE_URI = "vFlagImageUri";
    public static final String CUR_KEY_VISIBLE = "vVisible";
    public static final String CUR_KEY_ORDER = "vOrder";
    public static final String CUR_KEY_DIRECT = "vDirection";

    // Индексы столбцов таблицы валют
    public static final int VALINDEX_COLUMN = 0;
    public static final int VALCODE_COLUMN = 1;
    public static final int VALCHARCODE_COLUMN = 2;
    public static final int VALCURS_COLUMN = 3;
    public static final int VALNAME_COLUMN = 4;
    public static final int VALDATE_COLUMN = 5;
    public static final int FLAGURI_COLUMN = 6;
    public static final int VISIBLE_COLUMN = 7;
    public static final int ORDER_COLUMN = 8;
    public static final int VALCURS_DIRECT_COL_NUM = 9;

    // Имена столбцов таблицы металлов
    public static final String MET_KEY_ID = "_id";
    public static final String MET_KEY_CODE = "mCode";
    public static final String MET_KEY_PRICE = "mPrice";
    public static final String MET_KEY_DATE = "mDate";
    private static final String MET_KEY_IMAGE_URI = "mImageUri";
    public static final String MET_KEY_VISIBLE = "mVisible";
    public static final String MET_KEY_ORDER = "mOrder";
    public static final String MET_KEY_DIRECT = "mDirection";

    // Индексы столбцов таблицы металлов

    public static final int MET_ID_COL_NUM = 0;
    public static final int MET_CODE_COL_NUM = 1;
    public static final int MET_PRICE_COL_NUM = 2;
    public static final int MET_DATE_COL_NUM = 3;
    public static final int MET_IMAGE_COL_NUM = 4;
    public static final int MET_DIRECT_COL_NUM = 5;

    public static final String[] MET_ALL_COLUMNS = {MET_KEY_ID, MET_KEY_CODE, MET_KEY_PRICE, MET_KEY_DATE, MET_KEY_IMAGE_URI, MET_KEY_VISIBLE, MET_KEY_ORDER};




    protected static final String CREATE_CUR_TABLE = "create table " +
            CURRENCY_TABLE + " (" +
            CUR_KEY_ID + " integer primary key autoincrement, " +
            CUR_KEY_CODE + " ineger, " +
            CUR_KEY_CHARCODE + " text," +
            CUR_KEY_VCURS + " real," +
            CUR_KEY_VNAME + " text," +
            CUR_KEY_DATE + " long, " +
            CUR_KEY_IMAGE_URI + " text," +
            CUR_KEY_VISIBLE + " integer," +
            CUR_KEY_ORDER + " integer," +
            CUR_KEY_DIRECT + "integer" +
            ");";
    protected static final String [] INSERT_CURRENCY_DATA = {"insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('1', '36', 'AUD', '0', 'Австралийский доллар', '0', NULL, NULL, 3);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('2', '944', 'AZN', '0.0', 'Азербайджанский манат', '0', NULL, NULL, 4); ",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('3', '826', 'GBP', '0.0', 'Фунт стерлингов Соединенного королевства', '0', NULL, NULL, 2);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('4', '51', 'AMD', '0.0', 'Армянский драм', '0', NULL, NULL, 5);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('5', '974', 'BYR', '0.0', 'Белорусский рубль', '0', NULL, NULL, 6);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('6', '975', 'BGN', '0.0', 'Болгарский лев', '0', NULL, NULL, 7);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('7', '986', 'BRL', '0.0', 'Бразильский реал', '0', NULL, NULL, 8);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('8', '348', 'HUF', '0.0', 'Венгерский форинт', '0', NULL, NULL, 9);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('9', '208', 'DKK', '0.0', 'Датская крона', '0', NULL, NULL, 10);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('10', '840', 'USD', '0.0', 'Доллар США', '0', NULL, NULL, 0);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('11', '978', 'EUR', '0.0', 'Евро', '0', NULL, NULL, 1);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('12', '356', 'INR', '0.0', 'Индийская рупия', '0', NULL, NULL, 11);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('13', '398', 'KZT', '0.0', 'Казахский тенге', '0', NULL, NULL, 12);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('14', '124', 'CAD', '0.0', 'Канадский доллар', '0', NULL, NULL, 13);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('15', '417', 'KGS', '0.0', 'Киргизский сом', '0', NULL, NULL, 14);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('16', '156', 'CNY', '0.0', 'Китайский юань', '0', NULL, NULL, 15);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('17', '428', 'LVL', '0.0', 'Латвийский лат', '0', NULL, NULL, 16);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('18', '440', 'LTL', '0.0', 'Литовский лит', '0', NULL, NULL, 17);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('19', '498', 'MDL', '0.0', 'Молдавский лей', '0', NULL, NULL, 18);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('20', '578', 'NOK', '0.0', 'Норвежская крона', '0', NULL, NULL, 19);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('21', '985', 'PLN', '0.0', 'Польский злотый', '0', NULL, NULL, 20);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('22', '946', 'RON', '0.0', 'Новый румынский лей', '0', NULL, NULL, 21);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('23', '960', 'XDR', '0.0', 'СДР (специальные права заимствования)', '0', NULL, NULL, 22);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('24', '702', 'SGD', '0.0', 'Сингапурский доллар', '0', NULL, NULL, 23);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('25', '972', 'TJS', '0.0', 'Таджикский сомони', '0', NULL, NULL, 24);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('26', '949', 'TRY', '0.0', 'Турецкая лира', '0', NULL, NULL, 25);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('27', '934', 'TMT', '0.0', 'Новый туркменский манат', '0', NULL, NULL, 26);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('28', '860', 'UZS', '0.0', 'Узбекский сум', '0', NULL, NULL, 27);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('29', '980', 'UAH', '0.0', 'Украинская гривна', '0', NULL, NULL, 28);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('30', '203', 'CZK', '0.0', 'Чешская крона', '0', NULL, NULL, 29);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('31', '752', 'SEK', '0.0', 'Шведская крона', '0', NULL, NULL, 30);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('32', '756', 'CHF', '0.0', 'Швейцарский франк', '0', NULL, NULL, 31);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('33', '710', 'ZAR', '0.0', 'Южноафриканский рэнд', '0', NULL, NULL, 32);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('34', '410', 'KRW', '0.0', 'Вон Республики Корея', '0', NULL, NULL, 33);",
            "insert into curtable (\"_id\", \"vCode\", \"vchCode\", \"vCurs\", \"vName\", \"vDate\", \"vFlagImageUri\", \"vVisible\", \"vOrder\") values ('35', '392', 'JPY', '0.0', 'Японская иена', '0', NULL, NULL, 34);"};

    // Скрипт создания таблицы металлов

    protected static final String CREATE_METALL_TABLE = "create table " +
            METAL_TABLE + " (" +
            MET_KEY_ID + " integer primary key autoincrement, " +
            MET_KEY_CODE + " ineger, " +
            MET_KEY_PRICE + " real," +
            MET_KEY_DATE + " long, " +
            MET_KEY_IMAGE_URI + " text," +
            MET_KEY_VISIBLE + " integer," +
            MET_KEY_ORDER + " integer, " +
            MET_KEY_DIRECT + " integer" +
            ");";
    protected static final String[] INSERT_METAL_DATA = {"insert into metaltable (\"_id\", \"mCode\", \"mPrice\", \"mDate\", \"mImageUri\", \"mVisible\", \"mOrder\") values ('1', '1', '0.0', '0', NULL, NULL, NULL);",
            "insert into metaltable (\"_id\", \"mCode\", \"mPrice\", \"mDate\", \"mImageUri\", \"mVisible\", \"mOrder\") values ('2', '2', '0.0', '0', NULL, NULL, NULL);",
            "insert into metaltable (\"_id\", \"mCode\", \"mPrice\", \"mDate\", \"mImageUri\", \"mVisible\", \"mOrder\") values ('3', '3', '0.0', '0', NULL, NULL, NULL);",
            "insert into metaltable (\"_id\", \"mCode\", \"mPrice\", \"mDate\", \"mImageUri\", \"mVisible\", \"mOrder\") values ('4', '4', '0.0', '0', NULL, NULL, NULL);"};

    public static final String[] CUR_ALL_COLUMNS = {CUR_KEY_ID, CUR_KEY_CODE, CUR_KEY_CHARCODE, CUR_KEY_VCURS, CUR_KEY_VNAME, CUR_KEY_DATE, CUR_KEY_IMAGE_URI, CUR_KEY_VISIBLE, CUR_KEY_ORDER, CUR_KEY_DIRECT};

}
