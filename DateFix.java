package luceneProject;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by "P.Khodaparast" on 10/22/2016.
 */
public class DateFix {
    public static String fixSearchedDateResult(String dateStr){
        String[] str=dateStr.split("-");
        String date="";
        for (int i=0;i<str.length;i++) {
            if(i==2){
                str[2]=str[2].substring(0,2);
            }
            date=date+str[i];
        }
        return date;
    }
    public static HashMap fixSearchDate(int startMonth, int startDay, int endMonth, int endDay){
        String startMonthStr=startMonth+"";
        String startDayStr=startDay+"";
        String endMonthStr=endMonth+"";
        String endDayStr=endDay+"";
        if(startMonthStr.length()<2){
            startMonthStr="0"+startMonthStr;
        }
        if (startDayStr.length()<2){
            startDayStr="0"+startDayStr;
        }
        if(endMonthStr.length()<2){
            endMonthStr="0"+endMonthStr;
        }
        if (endDayStr.length()<2){
            endDayStr="0"+endDayStr;
        }
        HashMap date=new HashMap();
        date.put("startMonth",startMonthStr);
        date.put("startDay",startDayStr);
        date.put("endMonth",endMonthStr);
        date.put("endDay",endDayStr);
        return date;
    }

    public static void main(String[] args) {

    }
}
