package ru.openitr.cbrfinfo;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by
 * User: oleg
 * Date: 04.07.13
 * Time: 11:00
 */
public final class LogSystem {
    private static boolean mLoggingEnabled = true;
    private static final String PATH = "sdcard/log_cb_info.dat";
    public static boolean DEBUG = true;
    private LogSystem() {

    }

    public static int v(String tag, String msg) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.v(tag, msg);
        }
        return result;
    }

    public static int v(String tag, String msg, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.v(tag, msg, tr);
        }
        return result;
    }

    public static int d(String tag, String msg) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.d(tag, msg);
        }
        return result;
    }

    public static int d(String tag, String msg, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.d(tag, msg, tr);
        }
        return result;
    }

    public static int i(String tag, String msg) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.i(tag, msg);
        }
        return result;
    }

    public static int i(String tag, String msg, Throwable tr) {

        int result = 0;
        if (mLoggingEnabled) {
            result = Log.i(tag, msg, tr);
        }
        return result;
    }

    public static int w(String tag, String msg) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.w(tag, msg);
        }
        return result;
    }

    public static int w(String tag, String msg, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.w(tag, msg, tr);
        }
        return result;
    }

    public static int w(String tag, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.w(tag, tr);
        }
        return result;
    }

    public static int e(String tag, String msg) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.e(tag, msg);
        }
        return result;
    }

    public static int e(String tag, String msg, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.e(tag, msg, tr);
        }
        return result;
    }

    public static int logInFile(String tag, String msg)
    {
        if (!DEBUG) return 0;
        int result = 1;
        File file = new File(PATH);
        try {
            if (!file.exists()) {

                file.createNewFile();

            }
            String timeLog = new SimpleDateFormat("dd.MM.yy HH:mm:ss").format(new Date());
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.append(timeLog+" (" + tag + ")\t" + msg + "\n");
            bw.close();
            result = 0;
        } catch (IOException e) {
            e.printStackTrace();
            result = 1;
        }
        Log.d(tag,msg);
        return result;
    }

    public static int logInFile(String tag, Object object ,String msg){
        String msgWithClassName = getClassName(object)+ ": " + msg;
        return logInFile(tag, msgWithClassName);
    }

    public static String getClassName(Object object){
        return object.getClass().getSimpleName().toString();
    }

}
