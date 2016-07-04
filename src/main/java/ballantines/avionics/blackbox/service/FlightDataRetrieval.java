/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ballantines.avionics.blackbox.service;

import ballantines.avionics.blackbox.model.AircraftInformation;
import ballantines.avionics.blackbox.model.RouteInformation;
import java.util.Calendar;

/**
 *
 * @author mbuse
 */
public interface FlightDataRetrieval {
    
    /** @return ICAO airport code **/
    String getAirport();
    
    /** @return fuel in lb **/
    long getFuel();
   
    /** @return ground speed in kt **/
    double getGroundspeed();
    
    /** @return time, represented in UTC **/
    Calendar getTime();
    
    RouteInformation getRouteInformation();
    
    AircraftInformation getAircraftInformation();
    
}
