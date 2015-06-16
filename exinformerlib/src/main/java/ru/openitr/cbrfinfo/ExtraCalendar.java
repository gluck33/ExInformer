package ru.openitr.cbrfinfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by
 * User: oleg
 * Date: 02.08.13
 * Time: 15:31
 */
public class ExtraCalendar {

    public static boolean isToday(Calendar onDate){
        if (onDate == null) return false;
        Calendar today = Calendar.getInstance();
        return ((onDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))) & (onDate.get(Calendar.YEAR) == today.get(Calendar.YEAR));
    }

    public static boolean isFuture(Calendar onDate){
        if (onDate != null){
            Calendar today = Calendar.getInstance();
            return ((onDate.get(Calendar.DAY_OF_YEAR) > today.get(Calendar.DAY_OF_YEAR)) && (onDate.get(Calendar.YEAR) > today.get(Calendar.YEAR)));
        }
        return false;
    }

    public static boolean isPast(Calendar onDate){
        Calendar today = Calendar.getInstance();
        return ((onDate.get(Calendar.DAY_OF_YEAR) < today.get(Calendar.DAY_OF_YEAR)) && (onDate.get(Calendar.YEAR) < today.get(Calendar.YEAR)));
    }
    public static boolean isEqualDays(Calendar d1, Calendar d2){
        boolean yearIsEQ = d1.get(Calendar.YEAR) == d2.get(Calendar.YEAR);
        boolean dayOfYearIsEQ = d1.get(Calendar.DAY_OF_YEAR) == d2.get(Calendar.DAY_OF_YEAR);
        return yearIsEQ & dayOfYearIsEQ;

    }

    public static String getSimpleDateString(Calendar onDate){
        return new SimpleDateFormat("dd.MM.yy").format(onDate.getTime());
    }

    public static Calendar getToday(){
        return Calendar.getInstance();
    }
    public static Calendar getTomorrow(Calendar currentDate){
        currentDate.roll(Calendar.DAY_OF_YEAR, true);
        return currentDate;
    }
    public static Calendar getYesterday(Calendar currentDate){
        currentDate.roll(Calendar.DAY_OF_YEAR, -1);
        return currentDate;
    }

}
