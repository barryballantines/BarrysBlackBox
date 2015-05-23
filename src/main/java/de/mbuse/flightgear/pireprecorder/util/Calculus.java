/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mbuse.flightgear.pireprecorder.util;

/**
 *
 * @author mbuse
 */
public class Calculus {
    
    public static double max(double... values) {
        assert values.length > 0;
        
        double maxValue = values[0];
        for (int i=1; i<values.length; i++) {
            maxValue = Math.max(maxValue, values[i]);
        }
        return maxValue;
    }

    public static double average(double... values) {
        double sum = 0.0;
        for (double d : values) {
            sum += d;
        }
        return sum/values.length;
    }
}
