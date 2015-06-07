/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ballantines.avionics.blackbox;

import java.util.Calendar;

/**
 *
 * @author mbuse
 */
public interface FlightDataRetrieval {
    
    /** ICAO airport code **/
    String getAirport();
    
    /** fuel in lb **/
    long getFuel();
   
    /** ground speed in kt **/
    double getGroundspeed();
    
    /** time, represented in UTC **/
    Calendar getTime();
    
    RouteInformation getRouteInformation();
    
    AircraftInformation getAircraftInformation();
    
}
