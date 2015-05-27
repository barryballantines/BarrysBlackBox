/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ballantines.avionics.blackbox;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author mbuse
 */
public class Formats {
    public static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm 'UTC'");
    public static final NumberFormat TWO_DIGITS_FORMAT = new DecimalFormat("00");
    public static final NumberFormat INTEGER_FORMAT = DecimalFormat.getIntegerInstance();
    
    static {
        TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    } 
    
    public static String toUTC(Calendar cal) {
        return TIME_FORMAT.format(cal.getTime());
    }
    
    public static String secondsToHHMM(long seconds) {
        long t = seconds;
        long hours = (long) (t / (60 * 60));
        t = t - hours * (60 * 60);
        long minutes = (long) (t / 60);
        
        String duration = TWO_DIGITS_FORMAT.format(hours)
                + ":" + TWO_DIGITS_FORMAT.format(minutes);
        return duration;
    }
    
    public static String nauticalMiles(double miles) {
        return INTEGER_FORMAT.format(miles) + " nm";
    }
}
