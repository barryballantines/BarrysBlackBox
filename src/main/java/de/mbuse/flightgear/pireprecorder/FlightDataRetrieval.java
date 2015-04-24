/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.mbuse.flightgear.pireprecorder;

import java.util.Calendar;

/**
 *
 * @author mbuse
 */
public interface FlightDataRetrieval {
    
    String getAirport();
    
    long getFuel();
    
    Calendar getTimeUTC();
    
}
