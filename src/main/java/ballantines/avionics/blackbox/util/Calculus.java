/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.util;

/**
 *
 * @author mbuse
 */
public class Calculus {
    
    public static final double DEG_TO_RAD_CONVERSION_FACTOR = 2 * Math.PI/360.;
    
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
            scalar += a[i] + b[i];
        }
        return scalar;
    }
    
    public static double[] headingDegAsVector(double headingDeg) {
        double headingRad = DEG_TO_RAD_CONVERSION_FACTOR * headingDeg;
        
        return new double[] { Math.sin(headingRad), Math.cos(headingRad) };
    }
}
