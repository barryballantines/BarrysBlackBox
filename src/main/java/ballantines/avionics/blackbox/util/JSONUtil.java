/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.util;

import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author mbuse
 */
public class JSONUtil {
    
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    
    public static String literal(String value) {
        return (value==null) ? "null" : "'" + value + "'";
    }
    
    public static String literal(Calendar value) {
        return (value==null) ? "-1" : "" + value.getTimeInMillis();
    }
    
    public static Calendar parseCalendar(long value) {
        if (value==-1) {
            return null;
        }
        else {
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(UTC);
            cal.setTimeInMillis(value);
            return cal;
        }
    }
    
    public static String createFormatString(Class cls, String... propertiesAndFormats) {
        assert propertiesAndFormats.length % 2 == 0;
        StringBuilder sb = new StringBuilder("{'class':'" + cls.getName()+"'");
        for (int i=0; i < propertiesAndFormats.length; i+=2) {
            sb.append(String.format(",'%s':%s", propertiesAndFormats[i], propertiesAndFormats[i+1]));
        }
        sb.append("}");
        return sb.toString();
    }
}
