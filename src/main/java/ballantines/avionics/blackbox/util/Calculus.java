/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mbuse
 */
public class Calculus {
    
    public static final double DEG_TO_RAD_CONVERSION_FACTOR = 2 * Math.PI/360.;
    
    // N50°54'57.00" or E005°46'37.00"
    private static final Pattern DEGREE_PATTERN 
            = Pattern.compile("^([NSEW])(\\d+)°(\\d+)'(\\d+\\.\\d+)\"");
    
    public static double max(double... values) {
        assert values.length > 0;
        
        double maxValue = values[0];
        for (int i=1; i<values.length; i++) {
            maxValue = Math.max(maxValue, values[i]);
        }
        return maxValue;
    }
    
    public static double min(double... values) {
        assert values.length >0;
        double minValue = values[0];
        for (int i=1; i<values.length; i++) {
            minValue = Math.min(minValue, values[i]);
        }
        return minValue;
    }

    public static double average(double... values) {
        double sum = 0.0;
        for (double d : values) {
            sum += d;
        }
        return sum/values.length;
    }
    
    public static double scalar(double[] a, double[] b) {
        assert a.length == b.length;
        double scalar = 0.0;
        for (int i=0; i<a.length; i++) {
            scalar += (a[i] * b[i]);
        }
        return scalar;
    }
    
    public static double[] headingDegAsVector(double headingDeg) {
        double headingRad = DEG_TO_RAD_CONVERSION_FACTOR * headingDeg;
        
        return new double[] { Math.sin(headingRad), Math.cos(headingRad) };
    }
    
    public static double parseDegreeToDecimal(String degree) {
        Matcher matcher = DEGREE_PATTERN.matcher(degree);
        if (matcher.matches()) {
            String dir = matcher.group(1);
            double deg = (double) Integer.parseInt(matcher.group(2));
            double min = (double) Integer.parseInt(matcher.group(3));
            double sec = Double.parseDouble(matcher.group(4));
            
            double decimalDegree = deg + (min/60) + (sec/3600);
            
            if (dir.equals("N") || dir.equals("E")) {
                return decimalDegree;
            }
            else if (dir.equals("S") || dir.equals("W")) {
                return -decimalDegree;
            }
            else {
                return Double.NaN;
            }
        }
        else {
            return Double.NaN;
        }
    }
}
